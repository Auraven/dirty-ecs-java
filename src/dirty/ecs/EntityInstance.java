package dirty.ecs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class EntityInstance {
	public final int id;
	public final String entity_name;
	private HashMap<Class<? extends EntityComponent>, EntityComponent> component_map;
	private boolean enabled;
	private String name;
	private ArrayList<String> tags;
	
	public EntityInstance(int id, String entity_name) {
		this.id = id;
		this.entity_name = entity_name;
		component_map = new HashMap<Class<? extends EntityComponent>, EntityComponent>();
		enabled = true;
		name = "";
		tags = new ArrayList<String>();
	}
	public void reset() {
		for(EntityComponent component: component_map.values()) {
			component.reset();
		}
	}
	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
	}
	public void clearName(){
		name = "";
	}
	public void unname(){
		Dirty.unnameInstance(this);
	}

	public ArrayList<String> getTags(){
		return tags;
	}
	public void addTags(String... tags){
		this.tags.addAll(Arrays.asList(tags));
	}
	public void removeTags(String... tags){
		this.tags.removeAll(Arrays.asList(tags));
	}
	public void clearTags(){
		tags.clear();
	}

	public void untag(){
		Dirty.untagInstance(this);
	}

	public void addComponent(EntityComponent component) {
		component_map.put(component.getClass(), component);
	}
	public <T extends EntityComponent> T getComponent(Class<T> component_type) {
		return component_type.cast(component_map.get(component_type));
	}
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		if(enabled != this.enabled) {
			if(enabled) {
				Dirty.getEntity(entity_name).onEnable(this);
			}else {
				Dirty.getEntity(entity_name).onDisable(this);
			}
		}
		this.enabled = enabled;
	}
}