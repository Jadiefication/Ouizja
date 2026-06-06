use jni::{jni_sig, jni_str, EnvUnowned, JValue};
use jni::errors::{Error, ThrowRuntimeExAndDefault};
use jni::objects::{JByteArray, JClass, JDoubleArray, JFloatArray, JObject, JObjectArray};
use jni::sys::{jbyte, jdouble, jfloat, jint, jlong};
use crate::grid::Grid;

#[unsafe(no_mangle)]
pub unsafe extern "system" fn Java_io_jadieOuizjaLoader_createSim<'caller>(
    _class: JClass,
    mut env_unowned: EnvUnowned<'caller>,
    sourceMask: JDoubleArray,
    alphaMask: JDoubleArray,
    length: jint,
    height: jint
) -> jlong {
    env_unowned.with_env(|env| {
        if length < 1 || height < 1 {
            panic!("Invalid size")
        }
        let mut alpha_mask = vec![0.0f64; (length * height) as usize];
        let mut source_mask = vec![0.0f64; (length * height) as usize];

        alphaMask.get_region(env, 0, &mut alpha_mask)?;
        sourceMask.get_region(env, 0, &mut source_mask)?;

        let grid = Grid::new(source_mask, alpha_mask, length as usize, height as usize);
        let g_box = Box::new(grid);

        return Ok::<i64, Error>(Box::into_raw(g_box) as i64);
    }).resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub unsafe extern "system" fn Java_io_jadieOuizjaLoader_runSim<'caller>(
    _class: JClass,
    mut env_unowned: EnvUnowned<'caller>,
    iterations: jlong,
    pointer: jlong,
    length: jint,
    height: jint
) -> JObject<'caller> {
    env_unowned.with_env(|env| -> jni::errors::Result<JObject> {
        if length < 1 || height < 1 {
            panic!("Invalid size")
        }
        let raw_pointer = pointer as *mut Grid;

        let grid: &mut Grid = unsafe {
            assert!(
                !raw_pointer.is_null(),
                "Passed a null VM pointer from Kotlin!"
            );
            &mut *raw_pointer
        };

        grid.run(iterations as usize);

        let jni_arr = JObjectArray::<JDoubleArray>::new(env, length as usize, JDoubleArray::null())?;

        for (i, row_slice) in grid.temperature.chunks_exact(height as usize).enumerate() {
            let temp_arr = JDoubleArray::new(env, height as usize)?;
            temp_arr.set_region(env, 0, row_slice)?;
            jni_arr.set_element(env, i, temp_arr)?;
        }

        let class = env.find_class(jni_str!("io/jadie/SimState"))?;
        let object = env.new_object(
            class,
            jni_sig!("([[D)V"),
            &[
                JValue::Object(&jni_arr)
            ]
        )?;

        return Ok(object)
    }).resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub unsafe extern "system" fn Java_io_jadieOuizjaLoader_freeSim<'caller>(
    _class: JClass,
    _env_unowned: EnvUnowned,
    pointer: jlong
) {
    let raw_pointer = pointer as *mut Grid;

    unsafe {
        if !raw_pointer.is_null() {
            let _boxed_vm = Box::from_raw(raw_pointer);
        }
    }
}