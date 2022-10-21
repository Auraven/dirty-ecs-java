package dirty.ecs;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Dirty {

	private static HashMap<String, Entity> entity_map;
	private static HashMap<Integer, EntityInstance> instance_map;
	private static HashMap<String, ArrayList<Integer>> tagged_instances;	
	private static HashMap<String, Integer> named_instances;
	private static HashMap<String, ArrayList<EntityInstance>> recycle_map;
	
	private static HashMap<Class<? extends EntityComponent>, EntityComponent> component_map;
	
	private static HashMap<Class<? extends EntitySystem>, EntitySystem> system_map;
	private static HashMap<Float, HashMap<Integer, ArrayList<Class<? extends EntitySystem>>>> system_tick_map;
	private static HashMap<Float, Float> tickrate_map;
	
	private static Integer next_id;
		
	static {
		entity_map = new HashMap<String, Entity>();
		instance_map = new HashMap<Integer, EntityInstance>();
		tagged_instances = new HashMap<String, ArrayList<Integer>>();
		named_instances = new HashMap<String, Integer>();
		recycle_map = new HashMap<String, ArrayList<EntityInstance>>();
		
		component_map = new HashMap<Class<? extends EntityComponent>, EntityComponent>();		
		
		system_map = new HashMap<Class<? extends EntitySystem>, EntitySystem>();
		system_tick_map = new HashMap<Float, HashMap<Integer, ArrayList<Class<? extends EntitySystem>>>>();
		tickrate_map = new HashMap<Float, Float>();
		
		next_id = 0;
	}
	
	//Logic
	public static void pool() {
		for(EntitySystem system: system_map.values()) {
			for(Entity entity: entity_map.values()) {
				boolean valid = true;				
				for(Class<? extends EntityComponent> system_component_class: system.component_classes) {
					if(!entity.getComponentClasses().contains(system_component_class) && !entity.getGlobalComponentClasses().contains(system_component_class)) {
						valid = false;
						break;
					}
				}				
				if(valid) {
					system.addEntityName(entity.name);
				}
			}
			if(system.isEnabled()) {
				system.onEnable();
			}
		}
	}
	
	public static void update(float delta_time) {
		for(Float tickrate: tickrate_map.keySet()) {
			Float tick_count = tickrate_map.get(tickrate);
			tick_count += delta_time;
			
			if(tick_count >= tickrate) {
				tickrate_map.put(tickrate, 0f);
				for(ArrayList<Class<? extends EntitySystem>> system_classes: system_tick_map.get(tickrate).values()) {
					for(Class<? extends EntitySystem> system_class: system_classes) {
						EntitySystem system = system_map.get(system_class);
						if(system.isEnabled()) {
							system.preUpdate(delta_time);
							for(String entity_name: system.getEntityNames()) {
								ArrayList<EntityInstance> instances = new ArrayList<EntityInstance>();
								for(Integer instance_id: tagged_instances.get(entity_name)) {
									EntityInstance instance = instance_map.get(instance_id);
									if(instance.isEnabled()) {
										instances.add(instance);
									}
								}
								system.update(entity_map.get(entity_name), instances, delta_time);								
							}
							system.postUpdate(delta_time);
						}
					}
				}
			}else {
				tickrate_map.put(tickrate, tick_count);
			}
		}
	}
	
	
	//Entities
	public static void register(Entity entity) {
		entity_map.put(entity.name, entity);
		tagged_instances.put(entity.name, new ArrayList<Integer>());
		recycle_map.put(entity.name, new ArrayList<EntityInstance>());
	}
	
	public static Entity getEntity(String name) {
		return entity_map.get(name);
	}
	//Instances
	public static EntityInstance spawn(String entity_name) {
		Entity entity = entity_map.get(entity_name);
		EntityInstance instance = null;
		
		if(!recycle_map.get(entity_name).isEmpty()) {
			instance = recycle_map.get(entity_name).remove(0);
			instance.reset();
		}else {
			instance = new EntityInstance(next_id, entity_name);
			for(Class<? extends EntityComponent> component_class: entity.getComponentClasses()) {
				instance.addComponent(component_map.get(component_class).dupe());
			}
			next_id++;
		}
		
		instance_map.put(instance.id, instance);
		tagged_instances.get(entity_name).add(instance.id);
		
		entity.onSpawn(instance);
		
		return instance;
	}
	
	public static void despawn(Integer instance_id) {
		EntityInstance instance = instance_map.get(instance_id);
		
		instance_map.remove(instance_id);
		tagged_instances.get(instance.entity_name).remove(instance_id);
		
		recycle_map.get(instance.entity_name).add(instance);
		
		Entity entity = entity_map.get(instance.entity_name);
		entity.onDespawn(instance);
	}
	
	public static EntityInstance getInstanceByID(Integer instance_id) {
		return instance_map.get(instance_id);
	}
	
	public static void nameInstance(Integer instance_id, String name) {
		named_instances.put(name, instance_id);
	}
	
	public static void nameInstance(EntityInstance instance, String name) {
		nameInstance(instance.id, name);
	}
	
	public static void unnameInstance(String name) {
		named_instances.remove(name);
	}
	
	public EntityInstance getInstanceByName(String name) {
		int instance_id = named_instances.getOrDefault(name, -1);
		if(instance_id == -1) return null;
		return instance_map.get(instance_id);
	}
	
	public static void tagInstance(Integer instance_id, String tag) {
		tagged_instances.get(tag).add(instance_id);
	}
	
	public static void tagInstance(EntityInstance instance, String tag) {
		tagInstance(instance.id, tag);
	}
	
	public static void untagInstance(Integer instance_id, String tag) {
		tagged_instances.get(tag).remove(instance_id);
	}
	
	public static void untagInstance(EntityInstance instance, String tag) {
		untagInstance(instance.id, tag);
	}
	
	public static ArrayList<Integer> getInstancesByTag(String tag) {
		return tagged_instances.get(tag);
	}
	
	//Components
	public static void register(EntityComponent component) {
		component_map.put(component.getClass(), component);
	}
	
	public static <T extends EntityComponent> T getComponent(Class<T> component_type) {
		return  component_type.cast(component_map.get(component_type));
	}
	
	//Systems
	public static void register(EntitySystem system, Integer priority, Float tickrate) {
		if(!system_tick_map.containsKey(tickrate)) {
			system_tick_map.put(tickrate, new HashMap<Integer, ArrayList<Class<? extends EntitySystem>>>());
			tickrate_map.put(tickrate, 0f);
		}
		if(!system_tick_map.get(tickrate).containsKey(priority)) {
			system_tick_map.get(tickrate).put(priority, new ArrayList<Class<? extends EntitySystem>>());
		}
		
		system_map.put(system.getClass(), system);
		system_tick_map.get(tickrate).get(priority).add(system.getClass());
	}	
	
	public static void register(EntitySystem system, Integer priority){
		register(system, priority, 0f);
	}
	
	public static void register(EntitySystem system, Float tickrate){
		register(system, 4, tickrate);
	}
	
	public static void register(EntitySystem system){
		register(system, 4, 0f);
	}
	
	public static EntitySystem getSystem(Class<? extends EntitySystem> system_class) {
		return system_map.get(system_class);
	}
}