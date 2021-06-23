package de.dailab.oven.chatbot;

import zone.bot.vici.Skill;
import zone.bot.vici.SkillResolverService;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class ArcelikSkillResolver implements SkillResolverService {

	@Nonnull
	private final Set<Skill> skills = new HashSet<>();

	public ArcelikSkillResolver() {
		this.skills.add(new ArcelikSkill());
	}

	@Nonnull
	@Override
	public Set<Skill> getSkills() {
		return this.skills;
	}

}
