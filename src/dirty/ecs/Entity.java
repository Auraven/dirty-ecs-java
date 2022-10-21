package dirty.ecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Entity {
	public final String name;
	
	private ArrayList<Class<? extends EntityComponent>> component_classes;
	private HashMap<Class<? extends EntityComponent>, EntityComponent> global_component_map;
	private EntityListener listener;	
	
	@SafeVarargs
	public Entity(String name, Class<? extends EntityComponent>... component_classes) {
		this.name = name;
		this.component_classes = new ArrayList<Class<? extends EntityComponent>>();
		for(Class<? extends EntityComponent> component_class: component_classes) {
			this.component_classes.add(component_class);
		}
		global_component_map = new HashMap<Class<? extends EntityComponent>, EntityComponent>();
	}
	
	public Entity addGlobalComponents(EntityComponent... components) {
		for(EntityComponent component: components) {
			global_component_map.put(component.getClass(), component);
		}		
		return this;
	}
	
	public Entity setListener(EntityListener listener) {
		this.listener = listener;
		return this;
	}
	public EntityListener getListener() {
		return listener;
	}
	
	public void onEnable(EntityInstance instance) {
		if(listener != null) {
			listener.onEnable(this, instance);
		}
	}
	public void onDisable(EntityInstance instance) {
		if(listener != null) {
			listener.onDisable(this, instance);
		}
	}
	public void onSpawn(EntityInstance instance) {
		if(listener != null) {
			listener.onSpawn(this, instance);
		}
	}
	public void onDespawn(EntityInstance instance) {
		if(listener != null) {
			listener.onDespawn(this, instance);
		}
	}
	
	public <T extends EntityComponent> T getGlobalComponent(Class<T> component_type) {
		return component_type.cast(global_component_map.get(component_type));
	}
	public Set<Class<? extends EntityComponent>> getGlobalComponentClasses(){
		return global_component_map.keySet();
	}
	public ArrayList<Class<? extends EntityComponent>> getComponentClasses(){
		return component_classes;
	}
}