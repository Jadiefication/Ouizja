use jni::{jni_sig, jni_str, EnvUnowned, JValue};
use jni::errors::ThrowRuntimeExAndDefault;
use jni::objects::{JByteArray, JClass, JDoubleArray, JFloatArray, JObject, JObjectArray};
use jni::sys::{jbyte, jdouble, jfloat, jint, jlong};
use crate::grid::Grid;

#[unsafe(no_mangle)]
pub unsafe extern "system" fn Java_io_jadieOuizjaLoader_createSim<'caller>(
    _class: JClass,
    mut env_unowned: EnvUnowned<'caller>,
    baseTemperature: jfloat,
    sourceMask: JObjectArray,
    alphaMask: JObjectArray,
    length: jint,
    height: jint
) -> jlong {
    env_unowned.with_env(|env| {
        if length < 1 || height < 1 {
            panic!("Invalid size")
        }
        let mut rust_barrier_mask = [[false; height]; length];
        let mut rust_sources_mask = [[0.0f64; height]; length];
        for i in 0..(length as usize) {
            let inner_s_array: JByteArray = sourceMask.get_element(env, i).into();
            let inner_a_array: JDoubleArray = alphaMask.get_element(env, i).into();

            let mut temp_s_row = [0u8 as jbyte; height as usize];
            let mut temp_a_row = [0.0f64 as jdouble; height as usize];

            inner_s_array.get_region(env, 0, &mut temp_s_row)?;
            inner_a_array.get_region(env, 0, &mut temp_a_row)?;

            for j in 0..height {
                rust_barrier_mask[i][j] = temp_s_row[j] != 0;
                rust_sources_mask[i][j] = temp_a_row[j];
            }
        }

        let grid: Grid<{ length }, { height }> = Grid::new(baseTemperature as f32, rust_sources_mask, rust_barrier_mask);
        let g_box = Box::new(grid);

        return Ok(Box::into_raw(g_box) as i64);
    }).resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub unsafe extern "system" fn Java_io_jadieOuizjaLoader_runSim<'caller>(
    _class: JClass,
    mut env_unowned: EnvUnowned,
    iterations: jlong,
    pointer: jlong,
    length: jint,
    height: jint
) -> JObject<'caller> {
    env_unowned.with_env(|env| -> jni::errors::Result<JObject> {
        if length < 1 || height < 1 {
            panic!("Invalid size")
        }
        let raw_pointer = pointer as *mut Grid<{ length }, { height }>;

        let grid: &mut Grid<{ length }, { height }> = unsafe {
            assert!(
                !raw_pointer.is_null(),
                "Passed a null VM pointer from Kotlin!"
            );
            &mut *raw_pointer
        };

        grid.run(iterations as usize);

        let jni_arr = JObjectArray::new(env, length as usize, JObject::null())?;
        for i in 0..(length as usize) {
            let temp_arr = JFloatArray::new(env, height as usize)?;
            temp_arr.set_region(env, 0, &grid.field[i])?;
            jni_arr.set_element(env, i, temp_arr)?;
        }

        let class = env.find_class(jni_str!("io/jadie/SimState"))?;
        let object = env.new_object(
            class,
            jni_sig!("([[F)V"),
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
    pointer: jlong,
    length: jint,
    height: jint
) {
    if length < 1 || height < 1 {
        panic!("Invalid size")
    }
    let raw_pointer = pointer as *mut Grid<{ length }, { height }>;

    unsafe {
        if !raw_pointer.is_null() {
            let _boxed_vm = Box::from_raw(raw_pointer);
        }
    }
}