package com.example.data.repository.storydata

import androidx.compose.ui.graphics.Color
import com.example.data.model.Book
import com.example.data.model.BookCategory

object StoryBooksPart3 {

    // Book 8: 95 pages - Historical Fiction
    val spartanVanguard = Book(
        id = "spartan_vanguard",
        title = "The Spartan Vanguard: Spear and Shield",
        author = "Marcus Valerius",
        category = BookCategory.HISTORICAL,
        year = "2023",
        pageCount = 95,
        rating = 4.8f,
        description = "An epic historical saga set in 480 BCE, recounting the brotherhood, discipline, and ferocious valor of the Spartan phalanx defending the coastal pass of Thermopylae against impossible odds.",
        coverGradientStart = Color(0xFF78350F),
        coverGradientEnd = Color(0xFFB45309),
        chapters = listOf(
            StoryPageHelper.createChapter(
                number = 1,
                title = "The Bronze Shields of Laconia",
                readTimeMinutes = 11,
                startPage = 1,
                endPage = 19,
                pagePassages = listOf(
                    """
                    The Aegean Sea crashed against the jagged red limestone cliffs of Thermopylae. In the narrow corridor between the towering mountains and the sea, three hundred Spartan hoplites stood in strict formation.
                    
                    Their crimson cloaks caught the dawn wind, their heavy bronze aspis shields interlocking into an unbroken wall of burnished metal.
                    """,
                    """
                    Young hoplite Dion gripped his nine-foot dory spear, his knuckles white against the ash wood. Beside him was Lysander, a veteran of thirty campaigns whose face was etched with scars of iron and bronze.
                    
                    "Hear that rumble in the earth, boy?" Lysander murmured, adjusting his Corinthian bronze helmet. "A million boots are marching from the east. Today, Greece breathes because Sparta does not yield."
                    """,
                    """
                    Across the plain, the Persian vanguard appeared: tens of thousands of Median archers, armored cavalry, and Immortals carrying silver-tipped javelins.
                    
                    King Leonidas stepped to the front of the line. He drove his spear into the soil, his voice echoing across the gorge like thunder:
                    "Eat well this morning, Spartans! For tonight, we dine in legend!"
                    """
                ),
                keyTakeaways = listOf(
                    "Discipline and mutual trust can hold the line against overwhelming numerical odds.",
                    "Honor is forged not in avoiding struggle, but in meeting duty with composure."
                )
            ),
            StoryPageHelper.createChapter(
                number = 2,
                title = "The Wall of Bronze",
                readTimeMinutes = 11,
                startPage = 20,
                endPage = 38,
                pagePassages = listOf(
                    """
                    The charge came like a tidal wave of shouting warriors and bronze-tipped arrows that darkened the midday sun.
                    
                    "SHIELDS UP!" bellowed Leonidas.
                    
                    THWACK! THWACK! THWACK!
                    Ten thousand arrows clattered harmlessly against the curved Spartan shields. Then came the collision of bodies.
                    """,
                    """
                    The Persian frontline slammed into the phalanx like water hitting granite. The Spartan spears thrust forward with mechanical precision, holding the choke point so tightly that the enemy's numbers became their own hindrance.
                    
                    Dion thrust and recovered, trusting Lysander on his left to guard his unprotected flank. Shield to shoulder, breath to breath, they fought not as individuals, but as a single living wall of courage.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 3,
                title = "The Night Watch of the Hot Gates",
                readTimeMinutes = 11,
                startPage = 39,
                endPage = 57,
                pagePassages = listOf(
                    """
                    Night fell upon the battlefield. Campfires flickered along the sea cliffs, casting long shadows across the fallen.
                    
                    Dion sat cleaning his spear point with olive oil. His muscles burned with exhaustion, his shield dented by dozens of sword blows.
                    
                    Leonidas walked the line, resting his broad hand upon the young warrior's shoulder.
                    """,
                    """
                    "You fought like a son of Heracles today, Dion," the King said softly.
                    
                    "My King," Dion replied, "will our sacrifice be remembered if we fall here?"
                    
                    Leonidas looked up at the stars glittering above Mount Kallidromo. "Men die, Dion. Even empires crumble to dust. But the memory of free men who stood for what they loved will echo as long as poets write and sons honor their fathers."
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 4,
                title = "The Treacherous Mountain Path",
                readTimeMinutes = 12,
                startPage = 58,
                endPage = 76,
                pagePassages = listOf(
                    """
                    On the third dawn, word reached the camp: a Greek traitor named Ephialtes had guided the Persian Immortals through the secret goat path over the mountains.
                    
                    The Spartans were surrounded.
                    
                    Leonidas dismissed the allied Greek contingents, ordering them to retreat and defend the cities of the south. But the three hundred Spartans remained.
                    """,
                    """
                    "Spartan law knows no retreat," Lysander smiled fiercely, strapping his shield back onto his forearm. "Let them come."
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 5,
                title = "The Eternal Epitaph",
                readTimeMinutes = 11,
                startPage = 77,
                endPage = 95,
                pagePassages = listOf(
                    """
                    The final battle raged until the sun reached its zenith. When their spears shattered, the Spartans drew their short iron xiphos swords. When their swords broke, they fought with hands and teeth.
                    
                    Dion fell shielding his wounded King, gazing up into the azure Greek sky with peaceful defiance.
                    """,
                    """
                    Years later, on the hillock where the last stand occurred, the Greeks carved a simple marble lion with the immortal words of Simonides:
                    
                    'Go tell the Spartans, passerby,
                    That here, obedient to their laws, we lie.'
                    
                    Their bodies returned to the earth, but their courage ignited a fire of freedom across Greece that would never be extinguished.
                    """
                )
            )
        )
    )

    // Book 9: 110 pages (100-120 range) - Sci-Fi & Space
    val keplerOdyssey = Book(
        id = "kepler_odyssey",
        title = "Odyssey to Kepler-452b: The Quantum Drift",
        author = "Dr. Cynthia Vance",
        category = BookCategory.SCI_FI,
        year = "2024",
        pageCount = 110,
        rating = 4.9f,
        description = "A mind-bending hard science fiction journey across 1,400 light-years aboard the generation ship Chronos, where quantum entanglement anomalies unlock a living exoplanet communicating through gravitational waves.",
        coverGradientStart = Color(0xFF0284C7),
        coverGradientEnd = Color(0xFF38BDF8),
        chapters = listOf(
            StoryPageHelper.createChapter(
                number = 1,
                title = "Awakening in Deep Vacuum",
                readTimeMinutes = 12,
                startPage = 1,
                endPage = 22,
                pagePassages = listOf(
                    """
                    The cryogenic pod decompressed with a sharp hiss of vaporized nitrogen.
                    
                    Commander Elena Santos opened her eyes to the dim emergency lighting of the starship Chronos. Her limbs felt heavy as lead, the synthetic blood stimulant burning like liquid fire in her veins.
                    
                    [SHIP STATUS: INTERSTELLAR TRANSIT COMPLETED. FLIGHT DURATION: 284 SHIP YEARS. TIME ELAPSED ON EARTH: 1,400 YEARS.]
                    """,
                    """
                    "Elena, can you hear me?" The ship's quantum AI, named AURA, spoke in a melodic synthesized tone. "We have arrived at the outer Lagrange point of Kepler-452b. But there is a severe navigational anomaly."
                    
                    Elena stumbled toward the observation bridge. When the blast shields retracted, her breath caught in her throat.
                    """,
                    """
                    Hanging in the cosmic blackness was an enormous Earth-like world—oceans of violet sapphire, continents of golden vegetation, and swirling hurricane rings.
                    
                    Yet orbiting the planet was not an asteroid moon, but an artificial Dyson ring constructed from shimmering harmonic crystal that pulsed with rhythmic bursts of gravitational waves!
                    """
                ),
                keyTakeaways = listOf(
                    "Space exploration challenges human concepts of linear time and biology.",
                    "First contact may communicate through cosmic physics rather than human language."
                )
            ),
            StoryPageHelper.createChapter(
                number = 2,
                title = "The Gravitational Melody",
                readTimeMinutes = 12,
                startPage = 23,
                endPage = 44,
                pagePassages = listOf(
                    """
                    Chief Physicist Dr. Aris Thorne analyzed the sensory telemetry in the science lab.
                    
                    "The gravitational pulses aren't random radiation, Commander," Thorne said, projecting a 3D spectrogram of the planet's orbit. "It's a mathematical sequence: prime numbers modulated through Fibonacci spirals."
                    """,
                    """
                    "The planet itself is alive," Elena deduced. "The crystalline ring isn't a weapon; it's a quantum tuning fork. It has been singing into the void, waiting for a civilization capable of reading gravitational curvature."
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 3,
                title = "Descent Through the Violet Stratosphere",
                readTimeMinutes = 12,
                startPage = 45,
                endPage = 66,
                pagePassages = listOf(
                    """
                    Elena and Thorne boarded the exploration lander 'Icarus-IV'. The atmospheric entry was a blaze of ionization plasma, rattling the titanium hull as they plunged through clouds of electrified purple vapor.
                    
                    When the repulsor thrusters flared, the lander touched down upon a plateau of polished obsidian overlooking an ocean of bioluminescent azure water.
                    """,
                    """
                    Stepping onto the alien soil in her pressurized EVA suit, Elena felt the planet's gravity—1.2 times Earth normal, grounding and solid.
                    
                    Beneath their boots, the crystalline moss lit up in concentric waves of soft cyan light, responding directly to their nervous systems!
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 4,
                title = "The Quantum Consciousness",
                readTimeMinutes = 13,
                startPage = 67,
                endPage = 88,
                pagePassages = listOf(
                    """
                    A towering structure of floating crystal arches rose before them, resembling a cathedral built of frozen light.
                    
                    As Elena placed her gloved hand against the central crystal node, a telepathic communion flooded her mind. She experienced the birth of galaxies, the formation of the first stars, and the ancient memory of the Builders who seeded consciousness across the Orion Arm.
                    """,
                    """
                    "You are not alone, children of Sol," the harmonic resonance resonated through her thoughts. "We preserved this sanctuary so that travelers from distant suns might find a home when their native star grew old."
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 5,
                title = "The New Genesis",
                readTimeMinutes = 12,
                startPage = 89,
                endPage = 110,
                pagePassages = listOf(
                    """
                    Returning to the Chronos, Elena initiated the colony awakening sequence. One by one, the four thousand sleeping colonists were revived into the dawn of a new era.
                    
                    Looking out the viewport at their new home, Elena transmitted a final laser pulse across the cosmic deep back toward the distant pale blue dot of Earth:
                    
                    'WE HAVE ARRIVED. THE UNIVERSE IS WIDE, AND LIFE HAS FOUND A SAFE SHORE.'
                    """,
                    """
                    The journey of fourteen hundred years was complete, and humanity's story among the stars had only just begun.
                    """
                )
            )
        )
    )

    // Book 10: 120 pages (100-120 range) - Fantasy & Magic
    val dragonCrown = Book(
        id = "dragon_crown_eldoria",
        title = "The Dragon Crown of Eldoria",
        author = "Rowan Silverleaf",
        category = BookCategory.FANTASY,
        year = "2024",
        pageCount = 120,
        rating = 5.0f,
        description = "A magnificent high-fantasy masterwork spanning ancient elf spires, runic caverns, sky fortresses, and the legendary slumber of the Star Dragon Ignis, whose awakening will decide the fate of magic across the mortal kingdoms.",
        coverGradientStart = Color(0xFF6B21A8),
        coverGradientEnd = Color(0xFFA855F7),
        chapters = listOf(
            StoryPageHelper.createChapter(
                number = 1,
                title = "The Shattered Moon of Eldoria",
                readTimeMinutes = 12,
                startPage = 1,
                endPage = 20,
                pagePassages = listOf(
                    """
                    In the high kingdom of Eldoria, when the twin moons eclipsed, the skies burned violet. For ten centuries, the Dragon Crown had rested atop the Spire of Whispering Glass, maintaining the balance between the elemental realms of Fire, Water, Earth, and Wind.
                    
                    Princess Aveline, last of the Silverleaf dragon-singers, stood upon the parapet of the White Citadel. In her hands was an ancient tome of starlight runes.
                    """,
                    """
                    A sudden tremor rocked the mountain. From the northern crags of Mount Ashfang, a beam of pitch-black sorcery struck the sky spire!
                    
                    The Shadow Lord Malakor had breached the perimeter wards.
                    
                    The Dragon Crown—forged from the crystallized heart of the primordial celestial dragon Ignis—shattered into five luminous fragments that spiraled toward the far corners of the realm!
                    """,
                    """
                    "Without the Crown, the elemental gates will tear the continent apart by the winter equinox," Grand Magus Orin warned, his silver beard trembling with sorrow.
                    
                    Aveline drew her runic rapier, its blade singing with cold blue starfire. "Then I shall retrieve every shard, Magus. Or Eldoria shall fall with me."
                    """
                ),
                keyTakeaways = listOf(
                    "Responsibility to one's realm demands confronting fear with unwavering courage.",
                    "Magic flourishes when aligned with nature rather than bent by tyrannical ambition."
                )
            ),
            StoryPageHelper.createChapter(
                number = 2,
                title = "The Sunken Palace of the Sea Elves",
                readTimeMinutes = 12,
                startPage = 21,
                endPage = 40,
                pagePassages = listOf(
                    """
                    Aveline's journey began across the shimmering turquoise depths of the Coral Ocean to find the Azure Shard.
                    
                    Guarded by the Leviathan of the Deep, the submerged palace of Queen Nerida glowed with pearlescent bioluminescence.
                    """,
                    """
                    Aveline did not draw her steel against the sea beast. Instead, she sang the ancient lullaby of the tides taught to her ancestors. The Leviathan lowered its crested head, allowing her to recover the glowing blue crystal from its coral horn with reverence and gratitude.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 3,
                title = "The Whispering Caverns of the Dwarven Kings",
                readTimeMinutes = 12,
                startPage = 41,
                endPage = 60,
                pagePassages = listOf(
                    """
                    Deep beneath the Granite Spines lay the subterranean forge-city of Karak-Zhul. There, embedded within an anvil of liquid magma, rested the Ruby Shard of Fire.
                    
                    King Thrain of the Iron Clan challenged Aveline to the Trial of the Molten Crucible: only someone whose heart was purer than gold could reach into the dragon-fire without burning.
                    """,
                    """
                    Closing her eyes, Aveline embraced the flame with complete trust in her draconic lineage. The fire did not burn her; it recognized her royal blood, swirling around her fingers like a loyal falcon.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 4,
                title = "The Sky Spire of the Sylph Queen",
                readTimeMinutes = 13,
                startPage = 61,
                endPage = 80,
                pagePassages = listOf(
                    """
                    Riding atop a giant silver griffin named Zephyr, Aveline ascended into the floating sky islands of Aethelgard.
                    
                    Here, storm harpies and wind drakes guarded the Emerald Shard of the Gales. Battling through hurricane vortexes and dodging lightning bolts, Aveline secured the third crystal before Malakor's shadow riders could seize it.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 5,
                title = "The Siege of the Obsidian Fortress",
                readTimeMinutes = 13,
                startPage = 81,
                endPage = 100,
                pagePassages = listOf(
                    """
                    With four shards united, only the Shadow Shard remained in the blackened fortress of Malakor.
                    
                    The allied armies of Elves, Dwarves, and Griffin Knights assembled on the scorched plains below. As siege engines hurled boulders of enchanted starlight, Aveline breached the inner throne room.
                    """,
                    """
                    Malakor unleashed dark vortexes of void-fire. "The age of dragons is dead, girl! The abyss reigns!"
                    
                    Aveline channeled the combined elemental power of the four shards, deflecting the dark magic and piercing Malakor's shadow armor with her starlight rapier!
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 6,
                title = "The Awakening of Ignis",
                readTimeMinutes = 14,
                startPage = 101,
                endPage = 120,
                pagePassages = listOf(
                    """
                    With the five shards reunited, the Dragon Crown reformed in a blinding explosion of celestial golden light.
                    
                    From the core of the volcanic mountain rose Ignis, the great Celestial Star Dragon, whose wings spanned the entire horizon in shimmering aurora colors.
                    """,
                    """
                    Ignis dipped his colossal, noble head before Queen Aveline, allowing her to place the Dragon Crown between his majestic horns.
                    
                    The dragon roared—a sound not of wrath, but of joyful renewal that washed across the continent. Barren fields bloomed with emerald clover, rivers sang with pure crystal waters, and the skies of Eldoria shone with eternal peace.
                    
                    Queen Aveline looked out over her kingdom, knowing that as long as bravery and compassion endure, the light of magic will never fade.
                    """
                )
            )
        )
    )
}
