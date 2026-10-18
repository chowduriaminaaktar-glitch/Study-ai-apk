package com.example.data.repository.storydata

import androidx.compose.ui.graphics.Color
import com.example.data.model.Book
import com.example.data.model.BookCategory

object StoryBooksPart1 {

    // Book 1: 55 pages (50-60 range) - Fairy Tales & Fables
    val whisperingWoods = Book(
        id = "whispering_woods",
        title = "The Secret of the Whispering Woods",
        author = "Elena Thorne",
        category = BookCategory.FAIRY_TALE,
        year = "2024",
        pageCount = 55,
        rating = 4.9f,
        description = "A gentle, enchanting fairytale of a silver fox with starry eyes, an ancient oak that remembers the birth of dawn, and a village girl whose kindness restores harmony to the realm.",
        coverGradientStart = Color(0xFF047857),
        coverGradientEnd = Color(0xFF10B981),
        chapters = listOf(
            StoryPageHelper.createChapter(
                number = 1,
                title = "The Fox with Silver Fur",
                readTimeMinutes = 8,
                startPage = 1,
                endPage = 11,
                pagePassages = listOf(
                    """
                    Beyond the sleepy village of Oakhaven lay the Whispering Woods, a forest older than memory. The elders warned children never to cross the bramble brook after twilight, for the trees were said to hum tunes that lured wandering hearts into the mists.
                    
                    Twelve-year-old Lyra did not fear the woods. Her grandmother had taught her the speech of sparrows and the medicinal virtues of blue moss. On this autumn afternoon, while searching for wild winterberries, a sudden shimmer caught her eye.
                    
                    Curled beneath a canopy of golden ferns was a fox unlike any creature of the valley. Its fur shone like molten silver in the pale sunlight, and when it opened its luminous amber eyes, Lyra gasped. The fox did not flee.
                    """,
                    """
                    "You walk softly for a daughter of men," a voice chimed softly.
                    
                    Lyra stumbled back, clutching her wicker basket to her chest. "Did you... did you just speak?"
                    
                    The silver creature stood, stretching slender paws tipped in obsidian black. "Spoken words are merely thoughts given wind. My name is Kaelen, guardian of the Heart Tree. And you, Lyra of Oakhaven, have stepped into our circle because your heart harbors no snare."
                    
                    The wind rustled through the birch canopy above them, carrying a melody of chimes and distant waterfalls. Lyra knelt carefully, extending an open hand toward the fox.
                    """,
                    """
                    "Why have you revealed yourself to me?" Lyra asked, her voice trembling with wonder rather than dread.
                    
                    Kaelen's ears twitched toward the northern ridge. "Because the Great Oak is weeping sap of darkened amber. The Sun Stone at its roots has grown cold, and when the frost solstice arrives in three days, the warmth that shields this valley from the biting shadow will die."
                    
                    Lyra looked down at her hands—hands accustomed to weeding garden beds and mending wool coats. "I am just a village girl. How could I heal the ancient tree?"
                    
                    "Strength in our realm is not measured in iron swords," replied Kaelen gently, turning toward the deep woods. "It is measured in the truth you carry. Follow me, child of the green valley."
                    """
                ),
                keyTakeaways = listOf(
                    "Empathy and gentleness can bridge worlds where force and fear fail.",
                    "True courage often begins with listening rather than speaking."
                ),
                studyQuestions = listOf(
                    "Why does the silver fox choose Lyra rather than a warrior from the village?",
                    "What does the weeping Great Oak symbolize in the fable?"
                )
            ),
            StoryPageHelper.createChapter(
                number = 2,
                title = "The Trail of Luminous Moss",
                readTimeMinutes = 9,
                startPage = 12,
                endPage = 22,
                pagePassages = listOf(
                    """
                    Deeper into the woods, the twilight canopy turned a deep sapphire blue. Luminescent toadstools glowed like lanterns along the twisted roots of ancient cedar trees.
                    
                    "Step only where the emerald moss shines brightest," Kaelen cautioned, leaping across a babbling brook whose water sparkled with starlight. "The shadows here are hungry for footsteps."
                    
                    Lyra hopped from stone to stone. Her boots made no sound against the velvet moss. High above, tiny winged creatures—will-o'-the-wisps—danced in spiral circles, guiding their path.
                    """,
                    """
                    As they reached the Moonlit Glade, a towering figure blocked the narrow pass. It was an ancient stone golem, carved with vines that bloomed with pale night lilies.
                    
                    "None may pass who cannot answer the riddle of the roots," the stone keeper rumbled like falling pebbles.
                    
                    Kaelen stepped back, looking at Lyra. "This is your trial, young traveler."
                    
                    The golem's mossy eyes blinked slowly. "What grows richer the more it is shared, weighs nothing in your palm, yet warms the coldest hearth?"
                    """,
                    """
                    Lyra thought of her grandmother sitting beside the winter fireplace, wrapping a warm quilt around her shoulders and sharing stories of long-ago springs.
                    
                    "Kindness," Lyra answered firmly, stepping toward the guardian. "And the love we give without asking for a reward."
                    
                    The golem's weathered stone mouth curved into a warm smile. Slowly, the massive stone arms separated, and the guardian bowed. "Pass in peace, daughter of truth. You carry the spark the forest longs for."
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 3,
                title = "The Cavern of Reflections",
                readTimeMinutes = 9,
                startPage = 23,
                endPage = 33,
                pagePassages = listOf(
                    """
                    Beneath the roots of the northern mountains lay the Cavern of Reflections. Water dripped in rhythmic droplets from stalactites that looked like chandeliers of frozen light.
                    
                    In the center of the cavern sat a pool so still and polished it mirrored the cavern ceiling like a looking glass.
                    
                    "Look into the water, Lyra," whispered Kaelen. "Before you can touch the Sun Stone, you must see through the illusions of your own doubts."
                    """,
                    """
                    Lyra knelt beside the water's edge. In the reflection, she did not see herself. She saw the village laughing at her, whispering that she was foolish to dream of magical realms. She saw herself trapped in bitter winter snow, alone and forgotten.
                    
                    Tears welled in her eyes. "Is this what lies ahead?"
                    
                    "The pool shows what fear whispers, not what fate commands," the silver fox whispered, resting his warm snout against her wrist. "Look deeper."
                    """,
                    """
                    Lyra closed her eyes and took a deep breath. She remembered the warmth of the bread she baked for the sick elder, the robin she had rescued from the hawk, the steady beating of her own hopeful heart.
                    
                    When she reopened her eyes, the pool's water turned golden. In the water now reflected a thriving forest, green boughs heavy with blossoms, and children dancing safely under warm skies.
                    
                    "You have conquered your shadow," Kaelen smiled. "Now, the Heart Tree awaits."
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 4,
                title = "The Heart Tree of Elden",
                readTimeMinutes = 10,
                startPage = 34,
                endPage = 44,
                pagePassages = listOf(
                    """
                    At the center of the sanctuary stood the Great Oak of Elden. Its trunk was as wide as four cottages, its silver-veined leaves murmuring softly in a sacred breeze.
                    
                    Yet at its hollow core, a dark frost was creeping outward. The crystalline Sun Stone, nestled upon a pedestal of amber roots, had dimmed to a dull charcoal gray.
                    
                    "The Shadow Frost has almost reached the heartwood," Kaelen said solemnly. "If the stone turns black, eternal winter will freeze the forest and valley alike."
                    """,
                    """
                    A shadowy serpent made of freezing mist coiled around the base of the pedestal. It hissed with the sound of cracking river ice. "Turn back, mortals. The cold is pure; the cold feels no pain, no sorrow, no loss."
                    
                    Lyra stepped forward, feeling the icy draft stinging her cheeks. "The cold feels no sorrow, but it also knows no joy, no song, and no blooming spring."
                    
                    She reached into her satchel and pulled out the small glass vial of morning honey and wild peppermint she had brought from home.
                    """,
                    """
                    Pouring the fragrant sweetness onto the cold roots, Lyra placed both of her bare hands directly onto the frosted Sun Stone.
                    
                    "I offer my warmth," she declared softly, tears of compassion slipping from her cheeks onto the crystal. "Not because I am strong, but because love is never afraid to bleed for those in need."
                    
                    A brilliant pulse of crimson and gold flared from the stone, blinding the cavern in sunlit fire.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 5,
                title = "The Dawn of Eternal Spring",
                readTimeMinutes = 9,
                startPage = 45,
                endPage = 55,
                pagePassages = listOf(
                    """
                    The frost mist shattered like brittle glass. Warmth rippled outward in waves of golden light through every branch, leaf, and root of the Whispering Woods.
                    
                    Outside the cavern, the winter frost melted into singing rills. Wildflowers of violet, daffodil, and amber burst forth from the damp soil, greeting the morning sun.
                    
                    Kaelen's silver coat radiated like the full moon. He bowed his head gracefully before Lyra. "The realm is restored. And Oakhaven shall never know famine nor shadow again."
                    """,
                    """
                    Lyra walked back along the mossy path, her wicker basket now filled with golden acorns and fragrant forest blossoms that would never wither.
                    
                    As she stepped across the bramble brook into the meadows of her village, the morning bells rang out. The villagers gathered at the well, marveling at the sudden warm breeze and the sweet scent of honeysuckle in the air.
                    
                    Lyra turned back to catch one final glimpse of the tree line. At the forest edge, sitting gracefully upon a mossy boulder, the silver fox raised a paw in farewell before vanishing into the green embrace.
                    """,
                    """
                    From that day forward, whenever travelers asked why Oakhaven was the most peaceful and fertile village in all the provinces, the elders would smile and point toward the woods.
                    
                    "Because a little girl listened to a silver fox," they would say, "and remembered that true magic lives in kindness, honesty, and an open heart."
                    """
                )
            )
        )
    )

    // Book 2: 60 pages (50-60 range) - Comedy & Humor
    val professorPumpernickel = Book(
        id = "pumpernickel_time_machine",
        title = "The Accidental Time Machine of Professor Pumpernickel",
        author = "Barnaby Quirk",
        category = BookCategory.COMEDY,
        year = "2023",
        pageCount = 60,
        rating = 4.8f,
        description = "A riotous comic adventure about an eccentric bakery inventor whose modified toaster accidentally propels him into the Middle Ages with a tray of exploding cinnamon buns.",
        coverGradientStart = Color(0xFFD97706),
        coverGradientEnd = Color(0xFFFBBF24),
        chapters = listOf(
            StoryPageHelper.createChapter(
                number = 1,
                title = "The Chrono-Toaster Malfunction",
                readTimeMinutes = 8,
                startPage = 1,
                endPage = 12,
                pagePassages = listOf(
                    """
                    Professor Barnaby Pumpernickel was not a bad inventor; he was simply a baker who took thermodynamics far too personally.
                    
                    "The problem with modern sourdough," he announced to his bewildered basset hound, Wellington, "is that it lacks tachyonic acceleration!"
                    
                    In his cluttered kitchen laboratory in Upper Snodsbury, the Professor had wired a 1954 dual-slot chrome toaster to an antique brass grandfather clock, three copper coils, and an ominous car battery.
                    """,
                    """
                    "Watch closely, Wellington! When I depress the lever, the temporal flux will heat the pastry at precisely 800 years per minute, yielding the crispiest crust in Oxfordshire!"
                    
                    He shoved two blueberry scones into the slots. He donned his green safety goggles, cranked the dial from 'Light Toast' past 'Bagel' and straight into the red zone labeled 'DO NOT TURN UNDER PENALTY OF REALITY COLLAPSE'.
                    
                    CLACK!
                    """,
                    """
                    The toaster began to vibrate. It did not hum; it yodeled. Sparks of bright magenta leaped from the crumb tray.
                    
                    "Fascinating!" shouted Barnaby over the din. "The butter is achieving quantum resonance!"
                    
                    A vortex of purple smoke billowed from the slots. Before the Professor could reach for the fire extinguisher, the kitchen floor vanished beneath him with the sound of a giant cork popping out of a champagne bottle.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 2,
                title = "Landing in King Galahad's Stew",
                readTimeMinutes = 9,
                startPage = 13,
                endPage = 24,
                pagePassages = listOf(
                    """
                    SPLASH!
                    
                    Barnaby surfaced, gasping for air, clutching the smoking toaster above his head like a sacred chalice. He was neck-deep in warm liquid smelling heavily of boiled parsnips, leeks, and mutton fat.
                    
                    "Good heavens," he sputtered, wiping a cooked carrot slice from his goggles. "Either my septic tank has burst, or Oxfordshire has drastically deregulated its soup industry."
                    """,
                    """
                    Surrounding the giant iron cauldron were fifty men clad in iron chainmail, holding mutton chops and ale tankards in mid-air.
                    
                    At the head of the banquet table sat a burly man with a golden crown slightly askew on his greasy forehead.
                    
                    "By Saint Dunstan's beard!" the King roared, spilling wine over his ermine cloak. "A demon hath materialized in the royal pottage!"
                    """,
                    """
                    "Pardon me, good sir!" Barnaby said politely, climbing over the rim of the cauldron with a sodden squelch. "I am Professor Barnaby Pumpernickel. I assure you I am neither demon nor ghost, merely a fellow guildsman who took a rather aggressive shortcut through Tuesday afternoon."
                    
                    A dozen swords instantly unsheathed, glittering in the torchlight.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 3,
                title = "The Scone of Destiny",
                readTimeMinutes = 9,
                startPage = 25,
                endPage = 36,
                pagePassages = listOf(
                    """
                    "Seize the wizard!" shouted Sir Reginald the Grumpy. "He hath tainted the feast of Saint Crispin!"
                    
                    "Hold, hold!" Barnaby pleaded, reaching into his soaked coat pocket. "I bring gifts of peace! Behold, the miraculous pastry of the 21st century!"
                    
                    He held aloft one of the blueberry scones that had traveled through the wormhole. Due to chrono-acceleration, it was glowing with a faint, harmless neon blue light.
                    """,
                    """
                    The royal court gasped. The court wizard, a decrepit man named Morcar whose pointy hat had several moth holes, leaned forward with trembling hands.
                    
                    "Sire," Morcar quavered, "that is the legendary Mana Bread spoken of in the ancient parchment! He who eats it shall gain the wisdom of eagles!"
                    
                    The King eyed the pastry suspiciously. "Guards, make the royal taster eat it first."
                    """,
                    """
                    The royal taster, a nervous fellow named Higgins, took a hesitant nibble. His eyes widened. He chewed faster.
                    
                    "Sire!" Higgins proclaimed in ecstasy. "It tasteth like summer sunshine, crushed berries, and butter divine! It is heavenly!"
                    
                    The King snatched the rest of the scone and shoved it into his mouth. A look of supreme bliss washed over his bearded face. "The wizard stays! Knight him at once!"
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 4,
                title = "The Siege of Crumb Castle",
                readTimeMinutes = 10,
                startPage = 37,
                endPage = 48,
                pagePassages = listOf(
                    """
                    Barnaby's knighthood lasted exactly four hours before disaster struck.
                    
                    A trumpeter rushed into the great hall. "Sire! Duke Malakor the Unwashed has laid siege to the fortress with five hundred barbarians and a catapult!"
                    
                    The King turned to his newly minted knight. "Sir Pumpernickel of the Glowing Scone! You shall defeat them with your thunderous sorcery!"
                    """,
                    """
                    "My thunderous sorcery?" Barnaby squeaked. "Your Majesty, I make pastries! The most dangerous thing in my arsenal is an expired can of baking soda!"
                    
                    Then he paused. He looked at the royal pantry: barrels of fine white flour, jugs of vinegar, sulfur from Morcar's alchemy shelf, and his chrono-toaster.
                    
                    "Wait," Barnaby grinned. "Bring me fifty pounds of yeast, six barrels of raspberry jam, and every clean copper bucket in the castle!"
                    """,
                    """
                    When Duke Malakor charged the castle gate, he did not face arrows. He faced fifty rapid-fire flour cannons propelled by carbon dioxide yeast fermentation.
                    
                    BOOM! FZZZZ!
                    
                    Clouds of blinding flour covered the attackers, followed by hot sticky raspberry jam launched from high trebuchets. Within ten minutes, five hundred fierce warriors were covered in delicious glaze and completely unable to see or draw their bows.
                    
                    The enemy fled in sticky terror, leaving Duke Malakor crying for a napkin.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 5,
                title = "Toast to the Future",
                readTimeMinutes = 9,
                startPage = 49,
                endPage = 60,
                pagePassages = listOf(
                    """
                    King Galahad offered Barnaby half the kingdom, twenty sacks of gold, and the title of High Lord of the Rolling Pin.
                    
                    "A tempting offer, Your Majesty," Barnaby bowed with a chuckle, "but my basset hound Wellington needs his evening kibble, and my electric kettle does not take firewood."
                    
                    With the castle smithy helping him re-wire the copper coils, Barnaby set the toaster dial to 'REVERSE DEFROST'.
                    """,
                    """
                    He climbed onto the wooden catapult platform. "Farewell, noble knights! Remember: always let your dough proof for at least ninety minutes!"
                    
                    He pushed down the toaster lever.
                    
                    A flash of amber light enveloped him. The Middle Ages dissolved into a whirlpool of swirling lavender smoke.
                    """,
                    """
                    THUMP!
                    
                    Barnaby landed squarely in his armchair in Upper Snodsbury. The grandfather clock was chiming 5:02 PM. Wellington opened one lazy eye and thumped his tail against the rug.
                    
                    Beside the toaster sat two golden-brown pieces of bread, perfectly toasted with a subtle scent of medieval spices.
                    
                    Barnaby smiled, spread a dollop of strawberry jam, and murmured, "Now that is what I call a productive afternoon."
                    """
                )
            )
        )
    )

    // Book 3: 50 pages (50-60 range) - Inspiring & Morals
    val weaverOfLanterns = Book(
        id = "weaver_of_lanterns",
        title = "The Weaver of Golden Lanterns",
        author = "Master Kaelen Sun",
        category = BookCategory.INSPIRING,
        year = "2024",
        pageCount = 50,
        rating = 4.9f,
        description = "An inspiring and deeply moving moral journey of an elderly mountain artisan who teaches his restless apprentice that life's truest illumination comes from quiet perseverance and selfless service.",
        coverGradientStart = Color(0xFFB45309),
        coverGradientEnd = Color(0xFFF59E0B),
        chapters = listOf(
            StoryPageHelper.createChapter(
                number = 1,
                title = "The Workshop on the Mist Mountain",
                readTimeMinutes = 8,
                startPage = 1,
                endPage = 10,
                pagePassages = listOf(
                    """
                    High on the slopes of Mount Yun, where clouds drift through bamboo groves like silk ribbons, lived Old Chen the lantern maker. For sixty years, he had shaped fine bamboo ribs and stretched translucent rice paper over glowing tallow candles.
                    
                    Travelers who climbed the perilous mountain paths at night always found their way because Chen hung a golden lantern at every dangerous cliff edge.
                    """,
                    """
                    His apprentice, a spirited young boy named Ren, was impatient. Ren dreamed of traveling to the capital city to carve jade statues for emperors and earn purses of silver coin.
                    
                    "Master," Ren complained one evening while trimming bamboo splints, "we spend whole weeks shaping lanterns that travelers do not even look at. They take the light and walk away without a word of thanks. Why waste our lives for strangers who never remember our names?"
                    """,
                    """
                    Old Chen smiled serenely, smoothing the edge of a rice paper sheet with his calloused thumb.
                    
                    "A lighthouse does not blow trumpets to announce its presence to ships, Ren," the old man replied gently. "It simply burns through the storm so that others may live to reach the shore. True purpose does not seek applause; it seeks to be useful."
                    """
                ),
                keyTakeaways = listOf(
                    "Selfless work finds its greatest dignity in serving without demanding praise.",
                    "Patience and mastery grow in quiet, consistent dedication."
                ),
                studyQuestions = listOf(
                    "Why did Ren struggle to understand his master's dedication at first?",
                    "How does the metaphor of the lighthouse apply to modern life?"
                )
            ),
            StoryPageHelper.createChapter(
                number = 2,
                title = "The Storm of the Winter Solstice",
                readTimeMinutes = 8,
                startPage = 11,
                endPage = 20,
                pagePassages = listOf(
                    """
                    On the eve of the winter solstice, a furious blizzard descended upon Mount Yun. The wind shrieked like wolves, tearing at the cedar shingles of the workshop.
                    
                    Through the gale came the desperate clanging of the gorge warning bell. A caravan of merchants, including women and young children, had been caught on the Devil's Ridge pass.
                    
                    "The ice has covered the markers," Chen said, pulling on his heavy felt coat. "The bridge abyss is blind. We must carry the great lanterns to the ridge."
                    """,
                    """
                    Ren trembled as the icy wind shook the door. "Master! The wind will blow us into the canyon! It is madness to go out tonight!"
                    
                    Chen looked at his apprentice with eyes filled not with anger, but with profound sorrow. "If we do not go, forty souls will walk into the abyss tonight. Which fear weighs heavier on your conscience, Ren—the cold on your skin, or the memory of who you could have saved?"
                    """,
                    """
                    Without waiting for an answer, the old artisan stepped into the roaring blizzard, holding two lanterns shielded beneath his heavy oiled cloak.
                    
                    Ren stood in the warm workshop, the lantern flames reflecting in his conflicted eyes. He looked at his carving tools, then at the stormy darkness beyond the threshold.
                    
                    With a resolute shout, Ren seized the remaining four lanterns and plunged into the snow.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 3,
                title = "The Light on Devil's Ridge",
                readTimeMinutes = 9,
                startPage = 21,
                endPage = 30,
                pagePassages = listOf(
                    """
                    The trail was buried under knee-deep snow. Each gust threatened to sweep Ren off the jagged precipice. But ahead in the blinding whiteout, a small dot of amber light flickered steadfastly.
                    
                    It was Old Chen, anchoring the first lantern into the rock crevice at the edge of the chasm.
                    
                    "Ren!" Chen shouted over the storm, his beard crusted with frost. A smile of immense pride broke across the old man's face. "Plant the lanterns along the western cliff!"
                    """,
                    """
                    Working together against the shrieking gale, master and apprentice aligned the six golden lanterns. Their special oil, refined from pine sap and beeswax, burned with a steady, penetrating warmth that the storm could not extinguish.
                    
                    Through the blinding snow, the lead merchant's mule stopped inches before stepping into a three-hundred-foot drop.
                    
                    "Look! Look there!" the merchant cried. "The lights of the mountain guardian!"
                    """,
                    """
                    One by one, guided by the golden glow, the shivering travelers crossed the rope bridge to safety. Mothers held weeping children close, weeping with relief as they reached the mountain shelter.
                    
                    Ren watched from the shadows of the rocks. His hands were numb with cold, but inside his chest, a radiant warmth burned brighter than any fireplace.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 4,
                title = "The Master's Legacy",
                readTimeMinutes = 8,
                startPage = 31,
                endPage = 40,
                pagePassages = listOf(
                    """
                    When spring thawed the high passes, Old Chen fell ill. His breathing grew shallow, and his hands, once so nimble with bamboo, rested quietly upon the quilt.
                    
                    Ren sat beside his master's cot every hour, feeding him broth and reading poetry.
                    
                    "Ren," the old man whispered on a sunlit morning when plum blossoms drifted through the open window, "my lantern is burning to its wick. Do you still wish to leave for the capital?"
                    """,
                    """
                    Ren shook his head, holding his master's weathered hand tightly.
                    
                    "I used to think that greatness lived in palaces of gold and jade, Master," Ren answered with quiet conviction. "Now I know that the greatest honor on earth is to be the hand that keeps a traveler from falling in the dark."
                    """,
                    """
                    Chen smiled, his eyes twinkling with peaceful joy. "Then take the workshop, Ren. Make your lanterns not for glory, but for love. And as long as you keep the flame pure, you will never walk in darkness yourself."
                    
                    That evening, as the valley filled with twilight shadows, the master drew his final breath in peace.
                    """
                )
            ),
            StoryPageHelper.createChapter(
                number = 5,
                title = "The Everlasting Lantern",
                readTimeMinutes = 8,
                startPage = 41,
                endPage = 50,
                pagePassages = listOf(
                    """
                    Fifty years came and went on Mount Yun. Ren had grown into a wise elder, his hair as white as the winter snow, his hands bearing the same noble scars of craftsmanship as his master's.
                    
                    Beside him in the workshop, a young apprentice was complaining about the monotony of cutting bamboo splints.
                    
                    Ren smiled gently, just as Old Chen had smiled half a century before.
                    """,
                    """
                    "Listen, little one," Ren said softly, lighting the wick of a newly finished paper lantern. "Every lantern we craft carries a promise. We say to the world: 'No matter how fierce the blizzard, no matter how steep the cliff, someone cares enough to leave a light for you.'"
                    """,
                    """
                    That night, travelers climbing toward the high passes looked up at Mount Yun.
                    
                    Across the ridges and peaks, dozens of golden lanterns twinkled like fallen stars, guiding wanderers home through the silent, peaceful night.
                    """
                )
            )
        )
    )
}
