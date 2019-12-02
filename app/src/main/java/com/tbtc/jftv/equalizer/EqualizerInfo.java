package com.tbtc.jftv.equalizer;

public class EqualizerInfo {
	private int bandsNum;
	private int minLevel;
	private int maxLevel;
	private int[] level;
	private int[] minFreq;
	private int[] maxFreq;
	
	public int getBandsNum() {
		return this.bandsNum;
	}
	public int getMinLevel() {
		return this.minLevel;
	}
	public int getMaxLevel() {
		return this.maxLevel;
	}
	public int[] getMinFreq() {
		return this.minFreq;
	}
	public int[] getMaxFreq() {
		return this.maxFreq;
	}
	public int[] getLevel() {
		return this.level;
	}
	
	public EqualizerInfo(int iBandsNum, int iMinLevel, int iMaxLevel) {
		this.bandsNum = iBandsNum;
		this.minLevel = iMinLevel;
		this.maxLevel = iMaxLevel;
		
		if(iBandsNum < 1) iBandsNum = 0;
		this.level = new int[iBandsNum];
		this.minFreq = new int[iBandsNum];
		this.maxFreq = new int[iBandsNum];
		
		for(int i = 0; i < iBandsNum; i++) {
			this.level[i] = (this.maxLevel + this.minLevel) / 2;
		}
	}
	
	public void setInfo(int iBandsNum, int iMinLevel, int iMaxLevel) {
		this.bandsNum = iBandsNum;
		this.minLevel = iMinLevel;
		this.maxLevel = iMaxLevel;

		if(iBandsNum < 1) iBandsNum = 0;
		if(this.level.length != iBandsNum) {
			this.level = new int[iBandsNum];
			this.minFreq = new int[iBandsNum];
			this.maxFreq = new int[iBandsNum];	
			
			for(int i = 0; i < iBandsNum; i++) {
				this.level[i] = (this.maxLevel + this.minLevel) / 2;
			}
		} else {
			for(int i = 0; i < iBandsNum; i++) {
				if(this.level[i] < this.minLevel) this.level[i] = this.minLevel;
				else if(this.level[i] > this.maxLevel) this.level[i] = this.maxLevel;
			}
		}
	}
}
