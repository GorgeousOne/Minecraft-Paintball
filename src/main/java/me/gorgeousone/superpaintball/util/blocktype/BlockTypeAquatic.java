package me.gorgeousone.superpaintball.util.blocktype;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Wrapper for the block data of blocks after the aquatic update (1.13)
 */
public class BlockTypeAquatic extends BlockType {
	
	private final BlockData blockData;
	
	public BlockTypeAquatic(BlockTypeAquatic other) {
		blockData = other.blockData.clone();
	}
	
	public BlockTypeAquatic(BlockData data) {
		blockData = data.clone();
	}
	
	public BlockTypeAquatic(Material material) {
		this(material.createBlockData());
	}
	
	public BlockTypeAquatic(Block block) {
		this(block.getBlockData().clone());
	}
	
	public BlockTypeAquatic(BlockState state) {
		this(state.getBlockData().clone());
	}
	
	public BlockTypeAquatic(String serialized) {
		this(deserialize(serialized));
	}
	
	public static BlockData deserialize(String serialized) {
		BlockData data = Bukkit.createBlockData(serialized);
		
		if (serialized.contains("leaves") && !serialized.contains("persistent")) {
			data = data.merge(data.getMaterial().createBlockData("[persistent=true]"));
		}
		return data;
	}
	
	@Override
	public Material getType() {
		return blockData.getMaterial();
	}
	
	@Override
	public BlockType updateBlock(Block block, boolean applyPhysics) {
		BlockState oldState = block.getState();
		BlockState newState = block.getState();
		
		newState.setBlockData(blockData);
		newState.update(true, applyPhysics);
		return BlockType.get(oldState);
	}
	
	@Override
	public void sendBlockChange(Player player, Location location) {
		player.sendBlockChange(location, blockData);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(blockData);
	}
	
	@Override
	public BlockTypeAquatic clone() {
		return new BlockTypeAquatic(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BlockTypeAquatic)) {
			return false;
		}
		BlockTypeAquatic blockType = (BlockTypeAquatic) o;
		return blockData.equals(blockType.blockData);
	}
	
	@Override
	public String toString() {
		return blockData.getAsString(true);
	}
}