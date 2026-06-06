use crate::grid::Grid;
use jni::errors::{Error, ThrowRuntimeExAndDefault};
use jni::objects::{JBooleanArray, JClass, JDoubleArray, JObject, JObjectArray};
use jni::sys::{jint, jlong};
use jni::{jni_sig, jni_str, EnvUnowned, JValue};

#[unsafe(no_mangle)]
pub unsafe extern "system" fn Java_io_jadie_OuizjaLoader_createSim<'caller>(
    mut env_unowned: EnvUnowned<'caller>,
    _class: JClass,
    temps: JDoubleArray,
    sourceMask: JBooleanArray,
    alphaMask: JDoubleArray,
    nonSolidMask: JBooleanArray,
    length: jint,
    height: jint,
) -> jlong {
    env_unowned.with_env(|env| {
        if length < 1 || height < 1 {
            panic!("Invalid size")
        }
        let size = (length * height) as usize;

        let mut alpha_mask = vec![0.0f64; size];
        let mut temperatures = vec![0.0f64; size];
        let mut source_mask = vec![false; size];
        let mut non_solid_mask = vec![false; size];

        alphaMask.get_region(env, 0, &mut alpha_mask)?;
        temps.get_region(env, 0, &mut temperatures)?;
        sourceMask.get_region(env, 0, &mut source_mask)?;
        nonSolidMask.get_region(env, 0, &mut non_solid_mask)?;

        let grid = Grid::new(temperatures, source_mask, alpha_mask, length as usize, height as usize, non_solid_mask);
        let g_box = Box::new(grid);

        return Ok::<i64, Error>(Box::into_raw(g_box) as i64);
    }).resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub unsafe extern "system" fn Java_io_jadie_OuizjaLoader_runSim<'caller>(
    mut env_unowned: EnvUnowned<'caller>,
    _class: JClass,
    iterations: jlong,
    pointer: jlong,
    length: jint,
    height: jint,
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
            ],
        )?;

        return Ok(object);
    }).resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub unsafe extern "system" fn Java_io_jadie_OuizjaLoader_freeSim(
    _env_unowned: EnvUnowned,
    _class: JClass,
    pointer: jlong,
) {
    let raw_pointer = pointer as *mut Grid;

    unsafe {
        if !raw_pointer.is_null() {
            let _boxed_vm = Box::from_raw(raw_pointer);
        }
    }
}