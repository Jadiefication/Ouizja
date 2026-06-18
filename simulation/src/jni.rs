use crate::sim::cell::cell::Cell;
use crate::sim::cell::quantum::Quantum;
use crate::sim::grid::Grid;
use crate::sim::mask::Status::{Fusing, Gas, Liquid, Solid, Vaporizing};
use crate::sim::mask::{Mask, Status};
use crate::sim::material::Material;
use crate::sim::material::Material::{Air, Water};
use crate::sim::wind::Wind;
use haje::vec::vec2::Vec2;
use jni::errors::{Error, ThrowRuntimeExAndDefault};
use jni::objects::{JBooleanArray, JClass, JDoubleArray, JIntArray, JObject, JObjectArray};
use jni::sys::{jdouble, jint, jlong};
use jni::{EnvUnowned, JValue, jni_sig, jni_str};

/// JNI entry point to create a new simulation instance.
/// Returns a raw pointer to the `Grid` object as a `jlong`.
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
    tAmbient: jdouble,
) -> jlong {
    env_unowned
        .with_env(|env| {
            if length < 1 || height < 1 {
                panic!("Invalid size")
            }
            let size = (length * height) as usize;

            let mut temperatures = vec![0.0f64; size];
            let mut source_mask = vec![false; size];
            let mut partial_winds = vec![0.0f64; winds.len(env)?];
            let mut material_mask = vec![0; size];
            let q_len = quantum.len(env)?;
            let mut quantum_mask = vec![0.0; q_len];

            temps.get_region(env, 0, &mut temperatures)?;
            sourceMask.get_region(env, 0, &mut source_mask)?;
            winds.get_region(env, 0, &mut partial_winds)?;
            materialMask.get_region(env, 0, &mut material_mask)?;
            quantum.get_region(env, 0, &mut quantum_mask)?;

            let (w_chunks, _w_remainder) = if partial_winds.len() >= 3 {
                let (chunks, remainder) = partial_winds.as_chunks::<3>();
                (chunks, remainder)
            } else {
                (&[][..], &[][..])
            };
            let actual_winds: Vec<Wind> = w_chunks
                .into_iter()
                .map(|it| Wind {
                    force: Vec2 { x: it[0], y: it[1] },
                    temp: it[2],
                })
                .collect();

            let (q_chunks, q_remainder) = if q_len > 0 {
                let (chunks, remainder) = quantum_mask.as_chunks::<4>();
                if !remainder.is_empty() {
                    panic!("Remainder of quantum isn't 4^N")
                }
                (Some(chunks), remainder)
            } else {
                (None, &[][..])
            };

            let mut cells = temperatures
                .into_iter()
                .zip(source_mask)
                .zip(material_mask)
                .map(|((temp, source), id)| {
                    let material = Material::find_by_id(id as u8);
                    let status = if material == Air {
                        Gas
                    } else if material == Water {
                        Liquid
                    } else {
                        Solid
                    };
                    let props = material.thermal_properties();
                    let mask = Mask {
                        status,
                        source,
                        alpha: props.diffusivity,
                        material,
                        quantum: None,
                    };
                    let enthalpy = Cell::calculate_forward_enthalpy(temp, &props);
                    Cell { mask, enthalpy }
                })
                .collect::<Vec<Cell>>();

            if let Some(chunks) = q_chunks {
                chunks.iter().for_each(|it| {
                    let x = it[0] as i32;
                    let y = it[1] as i32;
                    if x >= 0 && x < length && y >= 0 && y < height {
                        let i = (x * height + y) as usize;
                        cells[i].mask.quantum = Some(Quantum {
                            gamma: 1.0,
                            kappa: it[2],
                            index: it[3] as i32,
                        })
                    }
                });
            }

            let grid = Grid::new(
                cells,
                length as usize,
                height as usize,
                actual_winds,
                tAmbient,
            );
            let g_box = Box::new(grid);

            return Ok::<i64, Error>(Box::into_raw(g_box) as i64);
        })
        .resolve::<ThrowRuntimeExAndDefault>()
}

/// JNI entry point to run the simulation for a specified number of iterations.
/// Returns a `SimState` object containing the results.
#[unsafe(no_mangle)]
pub unsafe extern "system" fn Java_io_jadie_OuizjaLoader_runSim<'caller>(
    mut env_unowned: EnvUnowned<'caller>,
    _class: JClass,
    iterations: jlong,
    pointer: jlong,
    length: jint,
    height: jint,
) -> JObject<'caller> {
    env_unowned
        .with_env(|env| -> jni::errors::Result<JObject> {
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

            let temps =
                JObjectArray::<JDoubleArray>::new(env, length as usize, JDoubleArray::null())?;
            let type_class = env.find_class(jni_str!("io/jadie/sim/Type"))?;

            let types = JObjectArray::<JObjectArray>::new(
                env,
                length as usize,
                JObjectArray::<JObject>::null(),
            )?;

            for (i, row_slice) in grid.cells.chunks_exact(height as usize).enumerate() {
                let temp_slice: Vec<f64> = row_slice
                    .iter()
                    .map(|cell| cell.get_temperature())
                    .collect();
                let temp_arr = JDoubleArray::new(env, height as usize)?;
                temp_arr.set_region(env, 0, &temp_slice)?;
                temps.set_element(env, i, temp_arr)?;

                let temp_arr = JObjectArray::<JObject>::new(env, height as usize, JObject::null())?;

                for (j, it) in row_slice.iter().enumerate() {
                    let status = it.mask.status;
                    let status_u8 = match status {
                        Solid => 0,
                        Liquid => 1,
                        Gas => 2,
                        Fusing { .. } => 3,
                        Vaporizing { .. } => 4,
                    };

                    let value = env
                        .call_static_method(
                            &type_class,
                            jni_str!("fromId"),
                            jni_sig!("(I)Lio/jadie/sim/Type;"),
                            &[JValue::Int(status_u8)],
                        )
                        .unwrap();

                    let obj = value.into_object().unwrap();
                    temp_arr.set_element(env, j, obj)?;
                }

                types.set_element(env, i, temp_arr)?;
            }

            let q_cells: Vec<Option<Cell>> = grid
                .cells
                .iter()
                .map(|&it| {
                    if it.mask.quantum.is_some() {
                        Some(it)
                    } else {
                        None
                    }
                })
                .collect();

            let q_length = q_cells.iter().filter(|it| it.is_some()).count();
            let q_states = JObjectArray::<JObject>::new(env, q_length, JObject::null())?;
            let mut write_idx = 0;

            for (i, cell) in q_cells.iter().enumerate() {
                if let Some(q_cell) = cell {
                    let t_class = env.find_class(jni_str!("kotlin/Triple"))?;
                    let i_class = env.find_class(jni_str!("java/lang/Integer"))?;
                    let d_class = env.find_class(jni_str!("java/lang/Double"))?;

                    let x = env.new_object(
                        &i_class,
                        jni_sig!("(I)V"),
                        &[JValue::Int((i as i32) / height)],
                    )?;
                    let y = env.new_object(
                        &i_class,
                        jni_sig!("(I)V"),
                        &[JValue::Int((i as i32) % height)],
                    )?;
                    let gamma = env.new_object(
                        &d_class,
                        jni_sig!("(D)V"),
                        &[JValue::Double(q_cell.mask.quantum.unwrap().gamma)],
                    )?;

                    let obj = env.new_object(
                        &t_class,
                        jni_sig!("(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"),
                        &[
                            JValue::Object(&x),
                            JValue::Object(&y),
                            JValue::Object(&gamma),
                        ],
                    )?;
                    q_states.set_element(env, write_idx, obj)?;
                    write_idx += 1;
                }
            }

            let class = env.find_class(jni_str!("io/jadie/SimState"))?;

            let object = env.new_object(
                &class,
                jni_sig!("([[D[Lkotlin/Triple;[[Lio/jadie/sim/Type;)V"),
                &[
                    JValue::Object(&temps),
                    JValue::Object(&q_states),
                    JValue::Object(&types),
                ],
            )?;

            return Ok(object);
        })
        .resolve::<ThrowRuntimeExAndDefault>()
}

/// JNI entry point to free the memory allocated for the simulation instance.
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
