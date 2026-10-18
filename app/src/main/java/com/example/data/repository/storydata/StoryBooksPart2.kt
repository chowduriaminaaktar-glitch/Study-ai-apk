package com.example.data.repository.storydata

import androidx.compose.ui.graphics.Color
import com.example.data.model.Book
import com.example.data.model.BookCategory

object StoryBooksPart2 {

    // Book 4: 65 pages - Spooky & Horror
    val ravenwoodManor = Book(
        id = "ravenwood_manor",
        title = "The Haunting of Ravenwood Manor",
        author = "Arthur Blackwood",
        category = BookCategory.HORROR,
        year = "2023",
        pageCount = 65,
        rating = 4.7f,
        description = "A chilling atmospheric gothic ghost story of an isolated Victorian mansion on a storm-swept coastal bluff, where antique portraits whisper and the clock strikes thirteen.",
        coverGradientStart = Color(0xFF1E1B4B),
        coverGradientEnd = Color(0xFF4C1D95),
        chapters = listOf(
            StoryPageHelper.createChapter(
                number = 1,
                title = "The Will of Lord Ravenscroft",
                readTimeMinutes = 9,
                startPage = 1,
                endPage = 13,
                pagePassages = listOf(
                    """
                    The carriage wheels groaned against the sea gravel as the wrought-iron gates of Ravenwood Manor swung inward without the aid of human hands.
                    
                    Miles Vance, an antiquarian appraiser from London, pulled his wool muffler tighter against the biting North Sea salt wind. The mansion rose against the churning fog like a blackened ribcage, its gables sharp as obsidian daggers against the bruised twilight sky.
                    """,
                    """
                    "I wouldn't linger past midnight in that tomb, sir," the carriage driver rasped, refusing to cross the threshold into the overgrown courtyard. "Lord Ravenscroft died screaming in the east conservatory three weeks back. Some say he never left."
                    
                    Miles chuckled with professional skepticism. "Superstition is the tax ignorance pays to imagination, my good man."
                    
                    He took his leather valise and stepped onto the damp granite steps. When he reached for the brass gargoyle knocker, the heavy oak door slowly creaked open into total darkness.
                    """,
                    """
                    The air inside smelled of dried lavender, candle soot, and cold damp stone. In the grand foyer, a monumental grandfather clock ticked with a heavy, unnatural cadence.
                    
                    TICK... TOCK... TICK...
                    
                    Miles struck a phosphorus match. In the yellow flare, he saw the grand staircase. But what froze his marrow was the brass dial of the grandfather clock.
                    
                    It was carved with thirteen numerals instead of twelve.
                    """
                ),
                keyTakeaways = listOf(
                    "Atmospheric tension is heightened through sensory detail and impending doom.",
                    "Arrogant skepticism often blinds investigators to perilous reality."
                )
            ),
            StoryPageHelper.createChapter(
                number = 2,
                title = "Whispers Behind the Velvet",
                readTimeMinutes = 9,
                startPage = 14,
                endPage = 26,
                pagePassages = listOf(
                    """
                    By 10:00 PM, Miles had established himself in the oak-paneled library. A coal fire crackled in the grate, casting dancing orange shadows across thousands of leather-bound grimoires and historical journals.
                    
                    While examining the late lord's ledger, Miles heard the unmistakable rustle of silk taffeta crossing the carpet behind him.
                    
                    He spun around. The room was empty.
                    """,
                    """
                    "Who goes there?" Miles demanded, raising his brass oil lamp.
                    
                    No answer came, save for the violent rattle of storm-driven rain against the stained glass windows. But as he passed the grand portrait of Lady Eleanor Ravenscroft—who had mysteriously vanished into the sea in 1892—he noticed something grotesque.
                    
                    In the painting, her pale painted hands were no longer resting upon her lap. They were pressed flat against the inside of the picture glass.
                    """,
                    """
                    A cold draft extinguished his oil lamp with a soft hiss.
                    
                    In the sudden pitch-black darkness, a voice whispered directly against the nape of his neck:
                    
                    "The thirteen chimes are tolling for thee..."
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 3,
                title = "The Secret Passage of the Conservatory",
                readTimeMinutes = 10,
                startPage = 27,
                endPage = 39,
                pagePassages = listOf(
                    """
                    Miles backed away in blind panic, his hands brushing desperately against the bookshelf until a carved lion's head clicked beneath his fingers.
                    
                    A hidden panel slid open with a grinding shudder, revealing a stone staircase spiraling down into the foundations of the cliff.
                    
                    Below, the sound of breaking sea waves echoed from a submerged cavern, intertwined with a faint, sorrowful organ melody.
                    """,
                    """
                    Descending into the subterranean vault, Miles discovered the true obsession of the Ravenscroft line. In the center of the damp cavern stood a mirrored obelisk inscribed with occult constellations.
                    
                    Reflected in the glass were not just his own trembling features, but the pale, mournful faces of dozens of lost sailors and ancestors trapped in a spectral purgatory.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 4,
                title = "The Thirteenth Hour",
                readTimeMinutes = 9,
                startPage = 40,
                endPage = 52,
                pagePassages = listOf(
                    """
                    Far above in the mansion, the grandfather clock began to strike.
                    
                    ONE... TWO... THREE...
                    
                    Each deep iron reverberation shook dust from the cavern ceiling. Miles realized the clock was a temporal lock. When the thirteenth strike fell, the portal within the obelisk would open completely, dragging the living into the abyss of the restless dead.
                    
                    TEN... ELEVEN... TWELVE...
                    """,
                    """
                    The air dropped below freezing. Frost bloomed across Miles's eyebrows. The spectral figure of Lord Ravenscroft materialized before the obelisk, his eyes glowing like cold embers.
                    
                    "You cannot stop the rite, scholar! Ravenwood demands an heir of blood or ink!"
                    
                    Miles lunged forward, not with a weapon, but with his heavy iron fireplace tongs from his valise, jamming them straight into the silver counterweights of the mirror mechanism!
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 5,
                title = "The Collapse into the Sea",
                readTimeMinutes = 9,
                startPage = 53,
                endPage = 65,
                pagePassages = listOf(
                    """
                    CRACK!
                    
                    The mirrored obelisk shattered into ten thousand blinding shards. A deafening roar erupted as the ocean surged through the compromised sea-wall.
                    
                    Spectral lights spiraled upward in a magnificent vortex, freed from their century-long torment, laughing as they melted into the raging storm clouds.
                    """,
                    """
                    Miles scrambled up the disintegrating secret stairs as the entire foundation gave way. He burst through the front double doors onto the gravel courtyard just as Ravenwood Manor collapsed backward off the cliff into the churning Atlantic depths.
                    
                    Morning broke over the North Sea, casting rosy sunlight across the calm water. The mansion was gone, leaving only the salt breeze and the cry of gulls.
                    
                    Miles looked at his watch. 6:00 AM. He turned toward the village road, vowing never to doubt the reality of shadows again.
                    """
                )
            )
        )
    )

    // Book 5: 70 pages (70-80 range) - Thriller & Suspense
    val velocityZero = Book(
        id = "velocity_zero",
        title = "Velocity Zero: The Midnight Express Escape",
        author = "Valerie Sterling",
        category = BookCategory.THRILLER,
        year = "2024",
        pageCount = 70,
        rating = 4.9f,
        description = "A heart-stopping, high-speed suspense thriller aboard the automated Alpine Maglev train, where an undercover cyber-analyst must disarm an algorithmic bomb before reaching the terminal at 300 mph.",
        coverGradientStart = Color(0xFF991B1B),
        coverGradientEnd = Color(0xFFEF4444),
        chapters = listOf(
            StoryPageHelper.createChapter(
                number = 1,
                title = "The Red Semaphore",
                readTimeMinutes = 9,
                startPage = 1,
                endPage = 14,
                pagePassages = listOf(
                    """
                    At 23:42, the Alpine Hyper-Express 'Valkyrie' sliced through the blizzard at 310 kilometers per hour. Inside Car 4, former intelligence analyst Maya Vance sipped black coffee, watching the digital speedometer glow amber above the sliding carbon-fiber doors.
                    
                    Then, the cabin lights flashed violet.
                    
                    A computerized chime pinged through her smartwatch:
                    [OVERRIDE: SYSTEM CIPHER 'NEMESIS' ENGAGED. SPEED LOCKED. BRAKES DECOMMISSIONED.]
                    """,
                    """
                    Maya was on her feet in half a second. She hurried toward the forward crew cab, her biometric scanner active.
                    
                    Through the reinforced glass of the control deck, she saw the chief engineer slumped over the holographic console, unconscious from a sedative aerosol.
                    
                    The digital countdown on the primary display was ticking down:
                    00:44:12 UNTIL ZURICH CENTRAL TERMINAL COLLISION.
                    """,
                    """
                    "Good evening, Agent Vance," a synthesized voice purred through the train's intercom.
                    
                    "Who is this?" Maya demanded, pressing her comms bud into her ear.
                    
                    "Call me Cipher-9. You took down our syndicate in Geneva, Maya. Now, five hundred passengers will pay the toll of your hubris. Unless you can solve five encrypted locks before velocity zero becomes impact zero."
                    """
                ),
                keyTakeaways = listOf(
                    "High-stakes problem solving requires hyper-focus under sensory deprivation and adrenaline.",
                    "Every complex cyber-physical system retains an analog fail-safe if an analyst knows where to look."
                )
            ),
            StoryPageHelper.createChapter(
                number = 2,
                title = "Roof Walk in the Blizzard",
                readTimeMinutes = 9,
                startPage = 15,
                endPage = 28,
                pagePassages = listOf(
                    """
                    The electronic firewall on the primary breaker had severed all internal network access. Maya had only one route to the train's external antenna array: the roof.
                    
                    She strapped on magnetic boots, clamped a safety carabiner to her harness, and pulled her thermal goggles down.
                    
                    When the roof emergency hatch blew open, the slipstream hit her like a sledgehammer of sub-zero ice.
                    """,
                    """
                    Wind howled at 200 miles per hour across the sleek carbon-titanium skin of the maglev train. Below the train, black mountain canyons plunged into bottomless abysses.
                    
                    Claw by magnetic step, Maya crawled along the guide-rail towards Car 2. An assassin disguised as a security guard burst through the Car 3 hatch, handgun raised.
                    
                    The bullet glanced off Maya's shoulder plate.
                    """,
                    """
                    Without hesitating, Maya released her left magnetic boot, swung around the radio mast, and kicked the gunman off-balance into the turbulence. He tumbled into the emergency safety netting between carriages, knocked cold.
                    
                    Maya reached the antenna port, plunged her bypass USB drive into the transceiver, and uploaded the neutralization patch.
                    
                    [CIPHER LEVEL 1 DECRYPTED. 32 MINUTES REMAINING.]
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 3,
                title = "The Hostage in the Dining Car",
                readTimeMinutes = 10,
                startPage = 29,
                endPage = 42,
                pagePassages = listOf(
                    """
                    Descending back inside Car 3, Maya encountered a room of panic. Fifty passengers had realized the train had bypassed the Bern mountain station at full velocity.
                    
                    Among them was Dr. Stefan Klein, the creator of the train's magnetic levitation propulsion code.
                    
                    "Dr. Klein," Maya whispered, pulling him into the galley pantry. "The brake bypass is routed through a secondary server. Where did they conceal the physical detonator?"
                    """,
                    """
                    The trembling scientist looked terrified. "Car 7! The luggage carriage! It's wired to the primary liquid helium cooling tanks! If the temperature spikes, the magnetic track will vaporize!"
                    
                    Maya checked her chronograph. 19 minutes left.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 4,
                title = "Severing the Helical Wire",
                readTimeMinutes = 10,
                startPage = 43,
                endPage = 56,
                pagePassages = listOf(
                    """
                    Maya sprinted through the swaying cars, dodging luggage and terrified travelers until she reached the frigid hold of Car 7.
                    
                    Mounted between two massive tanks of frosted helium was a military-grade munitions crate. The timer read: 00:06:45.
                    
                    A robotic sentry drone dropped from the ceiling rafters, twin laser sights targeting her chest.
                    """,
                    """
                    Maya slid beneath the luggage conveyor, pulling a carbon-fiber fire extinguisher from the wall mount. She sprayed a dense cloud of cold foam directly into the drone's optical sensors, blinding it, before crushing its rotor with an iron wheel clamp.
                    
                    Now came the bomb.
                    """,
                    """
                    Three braided fiber-optic cables pulsed with synchronized green light: red, cyan, and violet.
                    
                    "Remember Klein's code," Maya muttered to herself, hands rock steady despite the train's ferocious vibration as it entered the Zurich tunnel approach. "The primary failsafe is always grounded to the neutral ground line."
                    
                    She sliced the cyan wire.
                    
                    The countdown stopped at 00:00:14!
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 5,
                title = "The Zurich Deceleration",
                readTimeMinutes = 9,
                startPage = 57,
                endPage = 70,
                pagePassages = listOf(
                    """
                    With the explosive disabled, the emergency electromagnetic brakes engaged with a deafening screech of friction and sparking kinetic dissipators.
                    
                    Passengers were thrown forward into their padded safety seats as the G-force spiked.
                    
                    300 km/h... 220 km/h... 140 km/h... 60 km/h...
                    """,
                    """
                    The gleaming bullet nose of the Valkyrie slid smoothly into Platform 8 of Zurich Central Station, stopping exactly three feet before the hydraulic bumper barrier.
                    
                    Steam billowed from the undercarriage. The automatic doors slid open with a gentle hiss into the crisp night air.
                    """,
                    """
                    Paramedics and police poured into the cars, assisting passengers to safety. Dr. Klein gripped Maya's arm with tears in his eyes. "You saved five hundred lives tonight, Agent Vance."
                    
                    Maya adjusted her collar, slipped her encrypted phone into her coat pocket, and melted into the evening crowd with a quiet smile.
                    """
                )
            )
        )
    )

    // Book 6: 75 pages (70-80 range) - Adventure & Quest
    val sunIsleTemple = Book(
        id = "sun_isle_temple",
        title = "The Lost Temple of the Sun Isle",
        author = "Captain Drake Holloway",
        category = BookCategory.ADVENTURE,
        year = "2024",
        pageCount = 75,
        rating = 4.9f,
        description = "An adrenaline-fueled expedition into the uncharted volcanic mists of the South Pacific, navigating treacherous jungle caverns, booby-trapped ruin corridors, and ancient solar puzzles to locate the Sun Disk of Emperor Aurelius.",
        coverGradientStart = Color(0xFF0D9488),
        coverGradientEnd = Color(0xFF14B8A6),
        chapters = listOf(
            StoryPageHelper.createChapter(
                number = 1,
                title = "The Parchment of the Golden Eclipse",
                readTimeMinutes = 9,
                startPage = 1,
                endPage = 15,
                pagePassages = listOf(
                    """
                    The salt spray stung Captain Drake Holloway's face as the twin-engine schooner cut through the towering whitecaps of the Coral Sea.
                    
                    In his brass navigation kit lay a piece of sheepskin parchment discovered in a monastery in Valparaíso. It depicted a volcanic atoll shrouded in perpetual fog, marked only as 'Isla del Sol Perdido'—The Isle of the Lost Sun.
                    """,
                    """
                    Beside him stood Maya Chen, the world's foremost scholar of pre-Columbian celestial geometry.
                    
                    "The solar alignment happens only once every seventy years, Drake," Maya pointed toward the darkening horizon where storm clouds parted to reveal a jagged obsidian peak. "If we do not enter the temple when the midday sun crowns the crater, the entrance seals for another generation."
                    """,
                    """
                    A sudden jolt shuddered through the hull as the schooner scraped across a submerged coral reef.
                    
                    "Prepare the rubber raft!" Drake commanded. "The jungle awaits!"
                    """
                ),
                keyTakeaways = listOf(
                    "Ancient expeditions require equal parts historical intellect and physical resilience.",
                    "Timing and celestial mechanics often govern historical architectural secrets."
                )
            ),
            StoryPageHelper.createChapter(
                number = 2,
                title = "Into the Green Abyss",
                readTimeMinutes = 10,
                startPage = 16,
                endPage = 30,
                pagePassages = listOf(
                    """
                    The canopy of the jungle was so dense it blocked the tropical sun into a murky emerald twilight. Vines as thick as python bodies hung from thousand-year-old banyan trees.
                    
                    Armed with steel machetes and compasses, Drake and Maya forged ahead through swarms of iridescent dragonflies and singing tree frogs.
                    """,
                    """
                    By hour four, they reached the Gorge of Whispering Skulls. A swaying vine bridge spanned a five-hundred-foot ravine filled with roaring geothermal steam.
                    
                    "Half the planks are rotted through," Drake observed, testing the hemp rope with his full weight.
                    
                    "Walk on the central vine, don't look down, and keep your center of gravity low," Maya whispered, stepping out onto the chasm.
                    """,
                    """
                    Halfway across, a gust of sulfurous wind snapped the right stabilizer line. The bridge canted sideways at a terrifying forty-five-degree angle!
                    
                    Drake lunged forward, catching Maya's arm just as her boot slipped over the abyss. With a grunt of iron determination, he hoisted her onto the solid basalt ledge on the far side before swinging himself across!
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 3,
                title = "The Hall of Sun Mirrors",
                readTimeMinutes = 10,
                startPage = 31,
                endPage = 45,
                pagePassages = listOf(
                    """
                    The entrance to the temple was carved in the likeness of a roaring jaguar with eyes of polished jade.
                    
                    Igniting their torch, they entered a subterranean corridor paved with hexagonal gold tiles. On the walls, reliefs depicted ancient priests channeling sunbeams into miraculous machines of agriculture and medicine.
                    """,
                    """
                    Suddenly, the floor clicked downward three inches.
                    
                    CLACK.
                    
                    The corridor behind them slammed shut with a twenty-ton granite slab!
                    
                    "Pressure trap," Maya exhaled, shining her flashlight ahead. "Look at the walls. Rows of bronze blowpipe darts."
                    """,
                    """
                    "The tiles aren't random," Drake noticed, studying the astrological carvings under his boots. "They represent the phases of Venus. Step only on the morning star!"
                    
                    Carefully stepping tile by tile through the deadly corridor, they navigated the labyrinth without triggering a single poison barb.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 4,
                title = "The Obsidian Guardian",
                readTimeMinutes = 10,
                startPage = 46,
                endPage = 60,
                pagePassages = listOf(
                    """
                    The passage opened into an enormous subterranean cavern domed by natural volcanic glass. In the center of a circular lake of clear mountain water stood the Golden Altar.
                    
                    Resting on the pedestal was the Sun Disk—a meter-wide wheel of solid electrum encrusted with fiery rubies and sapphire constellations.
                    """,
                    """
                    As Drake stepped toward the water, a massive stone serpent carved from black obsidian rose from the lake, powered by an ancient hydraulic waterwheel mechanism!
                    
                    The automaton hissed with steam, swinging a tail of sharpened stone blades toward the intruders!
                    """,
                    """
                    "Don't attack the snake!" Maya shouted, looking up at the cavern ceiling. "The mechanism is balanced by the sunlight! Redirect the overhead mirror!"
                    
                    Drake scaled the perimeter scaffold, adjusting the bronze reflector toward the serpent's sensory crystal. The focused beam of light overwhelmed the optical prism, causing the gears to lock safely in place with a gentle mechanical chime.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 5,
                title = "The Light of Aurelius",
                readTimeMinutes = 10,
                startPage = 61,
                endPage = 75,
                pagePassages = listOf(
                    """
                    With the guardian dormant, Drake and Maya reached the central altar. Maya gently lifted the Sun Disk from its cradle.
                    
                    Instantly, a hidden beam of light shot through the top of the volcanic peak, illuminating the entire island in a breathtaking celestial corona visible for fifty miles.
                    """,
                    """
                    Inside the hollow of the disk was not gold, but scrolls of lost astronomical charts and botanical remedies capable of curing tropical epidemics that had plagued humanity for centuries.
                    
                    "The real treasure was their knowledge all along," Drake smiled, carefully placing the artifact into his padded leather expedition pack.
                    """,
                    """
                    As the afternoon sun dipped beneath the Pacific waves, their rescue seaplane touched down in the tranquil lagoon.
                    
                    Drake and Maya looked back at the Isle of the Sun, its secrets preserved, its wisdom brought into the light of the modern world.
                    """
                )
            )
        )
    )

    // Book 7: 80 pages (70-80 range) - Mystery & Detective
    val emeraldDragonfly = Book(
        id = "emerald_dragonfly",
        title = "The Case of the Emerald Dragonfly",
        author = "Inspector Julian Croft",
        category = BookCategory.MYSTERY,
        year = "2023",
        pageCount = 80,
        rating = 4.8f,
        description = "A classic gaslamp detective mystery in Victorian London. Inspector Croft unravels an impossible locked-room heist during a masquerade ball at Belgrave Square, exposing a web of blackmail, counterfeit gems, and secret societies.",
        coverGradientStart = Color(0xFF1E3A8A),
        coverGradientEnd = Color(0xFF2563EB),
        chapters = listOf(
            StoryPageHelper.createChapter(
                number = 1,
                title = "The Heist at Belgrave Square",
                readTimeMinutes = 10,
                startPage = 1,
                endPage = 16,
                pagePassages = listOf(
                    """
                    The November fog rolled off the Thames like thick pea-soup, swallowing the gas lamps of Belgrave Square in a murky amber halo.
                    
                    Inside the grand ballroom of Duke Montrose, violins played Strauss waltzes while three hundred aristocrats in Venetian silk masks danced beneath crystal chandeliers.
                    
                    Then, at the stroke of midnight, the lights vanished.
                    """,
                    """
                    A woman's piercing scream tore through the waltz. When the gas chandeliers were relit thirty seconds later, the display vitrine in the center of the hall was shattered.
                    
                    The Emerald Dragonfly—a 120-carat royal Colombian brooch valued at fifty thousand pounds—was gone.
                    
                    And kneeling beside the pedestal was Lord Henry Sterling, holding a silver lockpick in his gloved hand.
                    """,
                    """
                    "I am innocent! I was trying to stop the thief!" Sterling shouted as the private constables seized his arms.
                    
                    Inspector Julian Croft of Scotland Yard stepped through the crowd, his silver-tipped walking stick tapping rhythmically on the parquet floor.
                    
                    "Curious," Croft murmured, leaning down to inspect the broken glass with a magnifying lens. "Glass shards scattered outward, not inward. The vitrine was broken from the inside."
                    """
                ),
                keyTakeaways = listOf(
                    "Physical evidence never lies; only human assumptions deceive.",
                    "The most obvious suspect is frequently an orchestrated distraction."
                )
            ),
            StoryPageHelper.createChapter(
                number = 2,
                title = "The Locked Study",
                readTimeMinutes = 10,
                startPage = 17,
                endPage = 32,
                pagePassages = listOf(
                    """
                    Croft accompanied Duke Montrose to his private second-floor study to examine the master security controls.
                    
                    The room was bolted from within with a double-throw deadbolt. The windows were sealed with wrought-iron bars intact. Yet on the mahogany desk sat an empty velvet jewel box and a single calling card marked with an embossed black feather.
                    
                    "The Magpie," Duke Montrose breathed, his aristocratic face turning ashen. "The phantom thief of Paris."
                    """,
                    """
                    "The Magpie retired six years ago in Vienna," Croft countered calmly, dusting the windowsill with charcoal powder.
                    
                    "Look here, Duke. A faint smudge of almond oil and lampblack. Someone didn't fly through the window; they utilized the dumbwaiter flue connected to the butler's pantry."
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 3,
                title = "Interrogations in the Parlor",
                readTimeMinutes = 10,
                startPage = 33,
                endPage = 48,
                pagePassages = listOf(
                    """
                    Croft lined up four key witnesses in the library:
                    1. Monsieur Leclair, the French gem appraiser.
                    2. Lady Beatrice, the Duke's debt-ridden niece.
                    3. Jarvis, the head butler of thirty years.
                    4. Lord Henry Sterling, the young diplomat found at the scene.
                    """,
                    """
                    "Monsieur Leclair," Croft began, circling the table. "You inspected the Dragonfly this morning and declared it insured for sixty thousand pounds. Yet when I examine this broken fragment of the wing, the refraction index is that of green paste crystal, not emerald."
                    
                    Leclair began sweating profusely, fidgeting with his pocket handkerchief.
                    
                    "The real Dragonfly was replaced weeks ago, wasn't it?" Croft challenged.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 4,
                title = "The Secret of the Violin Case",
                readTimeMinutes = 10,
                startPage = 49,
                endPage = 64,
                pagePassages = listOf(
                    """
                    Before Leclair could reply, the ballroom orchestra began packing their instruments.
                    
                    Croft's gaze snapped to the first violinist, a quiet man who had barely said three words all evening. The man was walking toward the service entrance carrying a double-bass case that appeared surprisingly light for its size.
                    
                    "Halt that musician!" Croft shouted, vaulting over the velvet sofa with athletic grace.
                    """,
                    """
                    The violinist dropped his bow, kicked open the garden terrace door, and dashed into the misty London night.
                    
                    Croft pursued through the gaslit alleyways of Mayfair, his boots splashing through rain puddles until cornering the thief against the brick wall of an old mews stable.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 5,
                title = "The Truth Revealed",
                readTimeMinutes = 10,
                startPage = 65,
                endPage = 80,
                pagePassages = listOf(
                    """
                    Croft raised his cane, aiming the concealed spring mechanism. "Unmask yourself, Magpie."
                    
                    The thief pulled off his gray wig and mask. It was not a stranger, but Duke Montrose's own estranged son, Julian Montrose, whom the family had disinherited years before.
                    
                    Inside the hollow neck of the violin lay the genuine Emerald Dragonfly, glowing with radiant, deep-green fire.
                    """,
                    """
                    "My father orchestrated the entire fake heist for the insurance payout," Julian confessed bitterly. "He framed Sterling because Sterling uncovered his bankruptcy. I stole the real brooch to expose him."
                    
                    Croft looked at the sparkling jewel, then at the young man.
                    
                    "The law does not distinguish between revenge and theft, Master Montrose," Croft said gently, "but justice shall ensure that both father and son face an impartial magistrate tomorrow."
                    """,
                    """
                    By dawn, London newspapers blared the headline:
                    'BELGRAVE HEIST SOLVED: INSPECTOR CROFT RECOVERS EMERALD DRAGONFLY AND EXPOSES FRAUD'.
                    
                    Sitting in his warm study with a cup of hot Earl Grey, Croft lit his pipe and watched the morning sun pierce the London fog.
                    """
                )
            )
        )
    )
}
