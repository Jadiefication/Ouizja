use haje::vec::vec2::Vec2;
use crate::sim::grid::Grid;
use jni::errors::{Error, ThrowRuntimeExAndDefault};
use jni::objects::{JBooleanArray, JClass, JDoubleArray, JIntArray, JObject, JObjectArray};
use jni::sys::{jint, jlong};
use jni::{jni_sig, jni_str, EnvUnowned, JValue};
use crate::sim::cell::cell::Cell;
use crate::sim::cell::quantum::Quantum;
use crate::sim::mask::Mask;
use crate::sim::mask::Status::{Gas, Liquid, Solid};
use crate::sim::material::Material;
use crate::sim::material::Material::{Air, Water};
use crate::sim::wind::Wind;

#[unsafe(no_mangle)]
pub unsafe extern "system" fn Java_io_jadie_OuizjaLoader_createSim<'caller>(
    mut env_unowned: EnvUnowned<'caller>,
    _class: JClass,
    temps: JDoubleArray,
    sourceMask: JBooleanArray,
    materialMask: JIntArray,
    quantum: JDoubleArray,
    winds: JDoubleArray,
    length: jint,
    height: jint,
) -> jlong {
    env_unowned.with_env(|env| {
        if length < 1 || height < 1 {
            panic!("Invalid size")
        }
        let size = (length * height) as usize;

        let mut temperatures = vec![0.0f64; size];
        let mut source_mask = vec![false; size];
        let mut partial_winds = vec![0.0f64; winds.len(env)?];
        let mut material_mask = vec![0; size];
        let mut quantum_mask = vec![0.0; size];

        temps.get_region(env, 0, &mut temperatures)?;
        sourceMask.get_region(env, 0, &mut source_mask)?;
        winds.get_region(env, 0, &mut partial_winds)?;
        materialMask.get_region(env, 0, &mut material_mask)?;
        quantum.get_region(env, 0, &mut quantum_mask)?;

        let (w_chunks, w_remainder) = partial_winds.as_chunks::<3>();
        if !w_remainder.is_empty() {
            panic!("Remainder of winds isn't 3^N")
        }
        let actual_winds: Vec<Wind> = w_chunks.into_iter().map(|it| Wind {
            force: Vec2 { x: it[0], y: it[1] },
            temp: it[2]
        }).collect();

        let (q_chunks, q_remainder) = quantum_mask.as_chunks::<4>();
        if !q_remainder.is_empty() {
            panic!("Remainder of quantum isn't 4^N")
        }

        let mut cells = temperatures
            .into_iter()
            .zip(source_mask)
            .zip(material_mask)
            .map(|((temp, source), id)| {
                let material = Material::find_by_id(id as u8);
                let status = if material == Air { Gas } else if material == Water { Liquid } else { Solid };
                let props = material.thermal_properties();
                let mask = Mask {
                    status,
                    source,
                    alpha: props.diffusivity,
                    material,
                    quantum: None,
                };
                let enthalpy = Cell::calculate_forward_enthalpy(temp, &props);
                Cell {
                    mask,
                    enthalpy,
                }
            })
            .collect::<Vec<Cell>>();

        q_chunks.iter().for_each(|it| {
            let i = (((it[0] as i32) * height) + (it[1] as i32)) as usize;
            cells[i].mask.quantum = Some(Quantum {
                gamma: 1.0,
                kappa: it[2],
                index: it[3] as i32,
            })
        });

        let grid = Grid::new(cells, length as usize, height as usize, actual_winds);
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

        for (i, row_slice) in grid.cells.chunks_exact(height as usize).enumerate() {
            let temp_slice: Vec<f64> = row_slice
                .into_iter()
                .map(|cell| cell.get_temperature())
                .collect();
            let temp_arr = JDoubleArray::new(env, height as usize)?;
            temp_arr.set_region(env, 0, &temp_slice)?;
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