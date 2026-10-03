package com.example.data.repository

import androidx.compose.ui.graphics.Color
import com.example.data.model.Book
import com.example.data.model.BookCategory
import com.example.data.model.BookChapter

object LibraryCatalog {

    val books: List<Book> = listOf(
        Book(
            id = "meditations",
            title = "Meditations",
            author = "Marcus Aurelius",
            category = BookCategory.PHILOSOPHY,
            year = "180 AD",
            pageCount = 184,
            rating = 4.9f,
            description = "Timeless stoic reflections written by the Roman Emperor on personal discipline, resilience, ethics, inner peace, and mastering one's own mind.",
            coverGradientStart = Color(0xFFD97706),
            coverGradientEnd = Color(0xFFB45309),
            chapters = listOf(
                BookChapter(
                    number = 1,
                    title = "Debts and Lessons from Mentors",
                    readTimeMinutes = 6,
                    content = """
From my grandfather Verus, I learned good morals and the government of my temper. From the reputation and remembrance of my father, modesty and a manly character.

From my mother, piety and beneficence, and abstinence, not only from evil deeds, but even from evil thoughts; and further, simplicity in my way of living, far removed from the habits of the rich.

From my great-grandfather, not to have frequented public schools, and to have had good teachers at home, and to have understood that on such things a man should spend liberally.

From my governor, to be neither of the green nor of the blue faction at the circus, nor a partizan either of the Parmularius or the Scutarius at the gladiators' fights; from him too I learned endurance of labour, and to want little, and to work with my own hands, and not to meddle with other people's affairs, and not to be ready to listen to slander.

From Rusticus I received the impression that my character required improvement and discipline; and from him I learned not to be led astray to sophistic emulation, nor to writing on speculative subjects, nor to delivering little moralising addresses, nor to showing myself off as a man who does much discipline, or does benevolent acts in order to make a display.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Gratitude towards teachers and parents shapes foundational character.",
                        "Avoid vanity, theatrical moralizing, and unconstructive gossip.",
                        "Endurance and simplicity are essential buffers against adversity."
                    ),
                    studyQuestions = listOf(
                        "How does Marcus Aurelius view the influence of early role models?",
                        "What is the stoic definition of temperance illustrated here?"
                    )
                ),
                BookChapter(
                    number = 2,
                    title = "On the Morning Attitude and Human Nature",
                    readTimeMinutes = 8,
                    content = """
When you wake up in the morning, tell yourself: The people I deal with today will be meddling, ungrateful, arrogant, dishonest, jealous, and surly. They are like this because they cannot distinguish good from evil.

But I have seen the beauty of good, and the ugliness of evil, and have recognized that the wrongdoer has a nature related to my own—not of the same blood or birth, but the same mind, and possessing a share of the divine. And so none of them can hurt me. No one can implicate me in ugliness.

Nor can I feel angry at my fellow human, nor hate him. We were made to work together like feet, like hands, like the rows of the upper and lower teeth. To obstruct each other is contrary to nature. To be annoyed at also, to turn your back on him: these are obstructions.

Whatever this is that I am, it is a little flesh and breath, and the ruling part. Despise the flesh: blood and bones and a network, a jumble of nerves, veins, and arteries. Consider the breath: wind, always changing, expelled and sucked back again. The third part is the ruling master: you are an old person; do not let it be enslaved any longer, no more pulled like a puppet by every selfish impulse.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Prepare your mind each morning for interpersonal friction without resentment.",
                        "Other people's ignorance cannot damage your internal virtue unless you permit it.",
                        "Humans are designed for cooperation, not adversarial sabotage."
                    ),
                    studyQuestions = listOf(
                        "Why does the author argue that wrongdoers cannot truly harm him?",
                        "What does Marcus mean by the 'ruling part' of the soul?"
                    )
                ),
                BookChapter(
                    number = 3,
                    title = "The Inner Citadel and Impermanence",
                    readTimeMinutes = 7,
                    content = """
People try to get away from it all—to the country, to the beach, to the mountains. You always wish that you could too. Which is completely silly: you can get away from it all whenever you wish by going within.

Nowhere can human beings find a more peaceful, untroubled retreat than in their own soul. Especially if you have within you the principles to which you can turn at once and find yourself in complete tranquility—and by tranquility I mean nothing other than the proper ordering of the mind.

Keep two brief maxims always in mind: First, that things themselves cannot touch the soul at all, but stand motionless outside it; our disturbances come only from our own internal judgment. Second, that everything you see will change in a single moment and no longer be. Think constantly of how many changes you yourself have already witnessed.

The universe is flux; life is opinion.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "External travel cannot grant peace if the mind remains disordered.",
                        "External events lack moral quality; our value judgments create our stress.",
                        "Everything is in constant flux; acceptance of transience frees us from anxiety."
                    ),
                    studyQuestions = listOf(
                        "Explain the Stoic concept of the 'Inner Citadel'.",
                        "How does reflection on impermanence alleviate grief or anger?"
                    )
                )
            )
        ),

        Book(
            id = "calculus_essentials",
            title = "Calculus & Mathematical Analysis",
            author = "Academic Study Series",
            category = BookCategory.STEM,
            year = "Modern Edition",
            pageCount = 340,
            rating = 4.8f,
            description = "A rigorous, intuitive guide covering limits, differentiation, Riemann integration, multivariable calculus, and real-world physical applications.",
            coverGradientStart = Color(0xFF4F46E5),
            coverGradientEnd = Color(0xFF06B6D4),
            chapters = listOf(
                BookChapter(
                    number = 1,
                    title = "Limits, Continuity, and the ε-δ Foundation",
                    readTimeMinutes = 9,
                    content = """
Calculus begins with the foundational problem of continuous change. Before Isaac Newton and Gottfried Wilhelm Leibniz, mathematics could only measure static quantities. The breakthrough of calculus is the rigorous notion of the 'Limit'.

Formally, we say that the limit of f(x) as x approaches c is L, written:
lim (x -> c) f(x) = L

if for every real number ε > 0 (no matter how small), there exists a corresponding real number δ > 0 such that:
whenever 0 < |x - c| < δ, then |f(x) - L| < ε.

Continuity:
A function f is continuous at x = c if and only if three conditions are satisfied:
1. f(c) is defined (c is in the domain of f).
2. lim (x -> c) f(x) exists.
3. lim (x -> c) f(x) = f(c).

If a function is continuous on a closed interval [a, b], the Intermediate Value Theorem guarantees that f attains every value between f(a) and f(b). This ensures that continuous models reflect physical reality without sudden gaps.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Limits allow evaluating asymptotic behavior even where expressions are algebraically indeterminate (0/0).",
                        "The epsilon-delta definition provides a bulletproof topological guarantee of convergence.",
                        "Continuity on closed intervals yields the Intermediate Value and Extreme Value Theorems."
                    ),
                    studyQuestions = listOf(
                        "What is the geometric interpretation of epsilon and delta on a Cartesian plane?",
                        "Give an example of a function where the two-sided limit exists but the function is not continuous."
                    )
                ),
                BookChapter(
                    number = 2,
                    title = "The Derivative: Instantaneous Rate of Change",
                    readTimeMinutes = 10,
                    content = """
Geometrically, the derivative is the exact slope of the tangent line to the graph of a curve at any specific point. Physically, it represents the instantaneous rate of change.

The formal limit definition:
f'(x) = lim (h -> 0) [f(x + h) - f(x)] / h

Fundamental Rules of Differentiation:
1. Power Rule: d/dx [x^n] = n * x^(n - 1)
2. Product Rule: d/dx [u * v] = u'v + uv'
3. Quotient Rule: d/dx [u / v] = (u'v - uv') / v^2
4. Chain Rule: d/dx [f(g(x))] = f'(g(x)) * g'(x)

The Chain Rule is the linchpin of multivariable optimization and modern neural network backpropagation:
dy/dx = (dy/du) * (du/dx)

Higher-order derivatives reveal concavity and acceleration:
- First derivative f'(x) > 0 indicates an increasing function.
- Critical points occur where f'(x) = 0 or f'(x) is undefined.
- Second derivative f''(x) > 0 indicates concave upward (local minimum), while f''(x) < 0 indicates concave downward (local maximum).
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "The derivative converts secant slopes into tangent slopes as h approaches 0.",
                        "The Chain Rule allows decomposing nested composite functions systematically.",
                        "Extrema optimization is found at critical points where f'(x) = 0."
                    ),
                    studyQuestions = listOf(
                        "Derive the product rule using the limit definition of the derivative.",
                        "How is the Chain Rule used in machine learning gradient descent?"
                    )
                ),
                BookChapter(
                    number = 3,
                    title = "The Fundamental Theorem of Calculus",
                    readTimeMinutes = 11,
                    content = """
For centuries, the problem of tangents (differential calculus) and the problem of quadratures (finding the area under curves) were treated as completely unrelated branches of geometry.

The Fundamental Theorem of Calculus (FTC) proved the profound fact that Differentiation and Integration are exact inverse operations of one another.

Part 1 (The Rate of Accumulation):
If f is continuous on [a, b] and g(x) = ∫[a to x] f(t) dt, then g'(x) = f(x).
That is, the derivative of the accumulated area function is simply the original integrand function itself!

Part 2 (Evaluation of Definite Integrals):
If F is any antiderivative of f such that F'(x) = f(x), then:
∫[a to b] f(x) dx = F(b) - F(a).

This completely transformed mathematics: instead of summing infinite limits of approximating Riemann rectangles, one simply finds an antiderivative and evaluates it at the boundaries.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Part 1 connects accumulation of area to instantaneous rates.",
                        "Part 2 provides the practical computational tool for evaluating definite integrals.",
                        "Integration computes accumulated net change over intervals."
                    ),
                    studyQuestions = listOf(
                        "Why does the constant of integration C cancel out in definite integrals?",
                        "What is the physical meaning of integrating velocity with respect to time?"
                    )
                )
            )
        ),

        Book(
            id = "algorithms_illustrated",
            title = "Algorithms & Data Structures",
            author = "CS Core Series",
            category = BookCategory.COMPUTER_SCIENCE,
            year = "2026 Edition",
            pageCount = 290,
            rating = 4.9f,
            description = "Master time complexity, recursive problem solving, dynamic programming, graph traversals, and algorithmic system design.",
            coverGradientStart = Color(0xFF10B981),
            coverGradientEnd = Color(0xFF047857),
            chapters = listOf(
                BookChapter(
                    number = 1,
                    title = "Asymptotic Complexity & Big-O Notation",
                    readTimeMinutes = 7,
                    content = """
In computer science, comparing algorithms by measuring elapsed wall-clock seconds is flawed because hardware, operating systems, and background tasks vary wildly.

Instead, we use Asymptotic Notation (Big-O, Big-Omega, Big-Theta) to measure how the computational time or memory space scales as the input size n grows toward infinity.

Common Complexity Classes:
• O(1) - Constant: Array index lookup, hash table average lookup.
• O(log n) - Logarithmic: Binary search in a sorted array, balanced search tree operations.
• O(n) - Linear: Traversing an unsorted linked list, single pass array search.
• O(n log n) - Linearithmic: Merge sort, quicksort (average), heapsort.
• O(n^2) - Quadratic: Nested loops, bubble sort, brute force pair comparisons.
• O(2^n) - Exponential: Recursive Fibonacci without memoization, generating power sets.
• O(n!) - Factorial: Traveling Salesperson brute force permutation.

Rule of thumb for competitive programming and interview design:
If n <= 10^5, aim for O(n) or O(n log n).
If n <= 10^3, O(n^2) will execute in under 1 second.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Big-O measures the upper asymptotic bound of execution rate.",
                        "Constants and lower-order terms are dropped as n approaches infinity.",
                        "Choosing the correct data structure can turn an O(n^2) algorithm into an O(n log n) algorithm."
                    ),
                    studyQuestions = listOf(
                        "Why is merge sort guaranteed O(n log n) in all cases while quicksort worst-case is O(n^2)?",
                        "What is amortized time complexity in dynamic arrays (ArrayList/Vector)?"
                    )
                ),
                BookChapter(
                    number = 2,
                    title = "Trees, Graphs, and Traversals (BFS & DFS)",
                    readTimeMinutes = 10,
                    content = """
Non-linear data structures model hierarchical and networked relationships like filesystems, social networks, and routing tables.

Binary Search Tree (BST) Invariant:
For every node N:
- Every key in N's left subtree is strictly less than N.key.
- Every key in N's right subtree is strictly greater than N.key.

Graph Representation:
1. Adjacency Matrix: 2D array of size V x V. O(1) edge lookup, O(V^2) memory.
2. Adjacency List: Array or map of linked lists/vectors. Optimal for sparse graphs O(V + E) memory.

Graph Traversals:
• Depth-First Search (DFS):
  - Uses a Call Stack or explicit LIFO Stack.
  - Explores as deep as possible along each branch before backtracking.
  - Perfect for cycle detection, topological sorting, and maze pathfinding.

• Breadth-First Search (BFS):
  - Uses a FIFO Queue.
  - Explores nodes layer-by-layer in order of distance from starting root.
  - Guarantees finding the shortest path on unweighted graphs!
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "BFS is ideal for shortest paths in unweighted graphs.",
                        "DFS is ideal for topological sort, backtracking, and component discovery.",
                        "Always keep a visited set/array to avoid infinite loops in cyclic graphs."
                    ),
                    studyQuestions = listOf(
                        "How would you modify BFS to find the shortest path in a weighted graph? (Hint: Dijkstra's Algorithm)",
                        "What is the difference between an in-order, pre-order, and post-order traversal of a binary tree?"
                    )
                )
            )
        ),

        Book(
            id = "art_of_war",
            title = "The Art of War",
            author = "Sun Tzu",
            category = BookCategory.PHILOSOPHY,
            year = "5th Century BC",
            pageCount = 112,
            rating = 4.8f,
            description = "Ancient Chinese military treatise focusing on strategy, psychology, information superiority, deception, and victory without unnecessary bloodshed.",
            coverGradientStart = Color(0xFFEF4444),
            coverGradientEnd = Color(0xFF991B1B),
            chapters = listOf(
                BookChapter(
                    number = 1,
                    title = "Laying Plans and Calculating Odds",
                    readTimeMinutes = 6,
                    content = """
The art of war is of vital importance to the state. It is a matter of life and death, a road either to safety or to ruin. Hence it is a subject of inquiry which can on no account be neglected.

The art of war, then, is governed by five constant factors, to be taken into account in one's deliberations, when seeking to determine the conditions obtaining in the field:
1. The Moral Law: causes the people to be in complete accord with their ruler.
2. Heaven: signifies night and day, cold and heat, times and seasons.
3. Earth: comprises distances, great and small; danger and security; open ground and narrow passes.
4. The Commander: stands for the virtues of wisdom, sincerely, benevolence, strictness, and courage.
5. Method and Discipline: are to be understood the marshaling of the army in its proper subdivisions, the graduations of rank among the officers, and the maintenance of roads.

All warfare is based on deception.
Hence, when able to attack, we must seem unable; when using our forces, we must seem inactive; when we are near, we must make the enemy believe we are far away; when far away, we must make him believe we are near.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Thorough calculation before action determines victory before the conflict begins.",
                        "Deception and psychological positioning prevent adversaries from predicting your moves.",
                        "Moral alignment and clear discipline form the backbone of any collective enterprise."
                    ),
                    studyQuestions = listOf(
                        "How can the Five Factors be applied to modern competitive strategy or business?",
                        "What is Sun Tzu's warning regarding emotional decision making in leadership?"
                    )
                ),
                BookChapter(
                    number = 2,
                    title = "Attack by Stratagem and Knowing Yourself",
                    readTimeMinutes = 6,
                    content = """
In the practical art of war, the best thing of all is to take the enemy's country whole and intact; to shatter and destroy it is not so good.

Hence to fight and conquer in all your battles is not supreme excellence; supreme excellence consists in breaking the enemy's resistance without fighting.

Thus the highest form of generalship is to balk the enemy's plans; the next best is to prevent the junction of the enemy's forces; the next in order is to attack the enemy's army in the field; and the worst policy of all is to besiege walled cities.

If you know the enemy and know yourself, you need not fear the result of a hundred battles.
If you know yourself but not the enemy, for every victory gained you will also suffer a defeat.
If you know neither the enemy nor yourself, you will succumb in every battle.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "The pinnacle of strategy is achieving objectives without costly direct conflict.",
                        "Targeting the opponent's strategy is superior to brute-force confrontation.",
                        "Objective self-awareness combined with thorough intelligence ensures resilience."
                    ),
                    studyQuestions = listOf(
                        "Why does Sun Tzu consider besieging cities the worst policy?",
                        "Analyze the aphorism 'Know yourself and know your enemy' in the context of academic test preparation."
                    )
                )
            )
        ),

        Book(
            id = "principles_of_physics",
            title = "Principles of Classical & Modern Physics",
            author = "Scientific Academy",
            category = BookCategory.STEM,
            year = "Comprehensive Edition",
            pageCount = 380,
            rating = 4.9f,
            description = "From Newtonian kinematics, gravitation, and conservation laws to electromagnetic fields, thermodynamics, and special relativity.",
            coverGradientStart = Color(0xFF0284C7),
            coverGradientEnd = Color(0xFF0369A1),
            chapters = listOf(
                BookChapter(
                    number = 1,
                    title = "Newtonian Dynamics & Conservation Laws",
                    readTimeMinutes = 9,
                    content = """
Classical mechanics describes the motion of macroscopic objects subject to forces.

Newton's Three Laws of Motion:
1. Law of Inertia: An object at rest remains at rest, and an object in uniform motion continues in a straight line at constant velocity, unless acted upon by a net external force (ΣF = 0 implies a = 0).
2. Law of Acceleration: The net force acting on a body is directly proportional to its rate of change of linear momentum:
   F_net = dp/dt = d(mv)/dt = m * a (for constant mass).
3. Action and Reaction: For every action, there is an equal and opposite reaction (F_A_on_B = -F_B_on_A).

Conservation of Mechanical Energy:
In the presence of conservative forces (such as gravity and ideal springs), total mechanical energy is conserved:
E_total = Kinetic Energy + Potential Energy = (1/2)mv^2 + mgh = Constant.

Conservation of Linear Momentum:
When no external force acts on a closed system (ΣF_ext = 0), the total momentum before collision equals the total momentum after collision:
m1*v1_initial + m2*v2_initial = m1*v1_final + m2*v2_final.
This holds true regardless of whether collisions are elastic (conserving kinetic energy) or inelastic (converting kinetic energy to thermal energy or deformation).
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Forces always occur in paired interactions across distinct objects.",
                        "Conservation laws allow solving complex collision and motion problems without tracking instantaneous forces.",
                        "Momentum is always conserved in isolated systems, even during inelastic collisions."
                    ),
                    studyQuestions = listOf(
                        "Why does a person jumping off a boat push the boat backwards?",
                        "Differentiate between elastic and perfectly inelastic collisions using energy equations."
                    )
                ),
                BookChapter(
                    number = 2,
                    title = "Special Relativity and Mass-Energy Equivalence",
                    readTimeMinutes = 11,
                    content = """
In 1905, Albert Einstein published his paper 'On the Electrodynamics of Moving Bodies', dismantling classical assumptions about absolute time and space.

The Two Postulates of Special Relativity:
1. The Principle of Relativity: The laws of physics are identical in all inertial (non-accelerating) reference frames.
2. The Constancy of the Speed of Light: Light propagates through vacuum with a definite speed c (≈ 3 × 10^8 m/s), independent of the motion of the emitting source or the observer!

Consequences of the Postulates:
• Time Dilation: Moving clocks run slower relative to stationary observers:
  Δt = Δt0 / √(1 - v^2 / c^2) = γ * Δt0
  where γ (Lorentz factor) = 1 / √(1 - v^2/c^2).

• Length Contraction: Objects moving at relativistic speeds are measured to be shorter along their direction of motion:
  L = L0 * √(1 - v^2 / c^2) = L0 / γ.

• Mass-Energy Equivalence:
  E = m * c^2
  Mass is simply concentrated, dormant energy. In nuclear reactions, a tiny mass defect Δm yields enormous energy Release E = Δm * c^2.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Time and space are not absolute; they are interwoven into a dynamic 4-dimensional spacetime continuum.",
                        "No physical object with rest mass can accelerate to or exceed the speed of light c in vacuum.",
                        "Mass and energy are fundamentally interchangeable manifestations of the same physical property."
                    ),
                    studyQuestions = listOf(
                        "Explain how atmospheric muon decay provides experimental proof of time dilation.",
                        "What happens to the Lorentz factor γ as velocity v approaches c?"
                    )
                )
            )
        ),

        Book(
            id = "frankenstein",
            title = "Frankenstein; or, The Modern Prometheus",
            author = "Mary Shelley",
            category = BookCategory.LITERATURE,
            year = "1818",
            pageCount = 280,
            rating = 4.7f,
            description = "The pioneering gothic science-fiction masterpiece exploring human hubris, the limits of science, alienation, creation, and moral responsibility.",
            coverGradientStart = Color(0xFF6B21A8),
            coverGradientEnd = Color(0xFF3B0764),
            chapters = listOf(
                BookChapter(
                    number = 1,
                    title = "The Spark of Creation",
                    readTimeMinutes = 8,
                    content = """
It was on a dreary night of November that I beheld the accomplishment of my toils. With an anxiety that almost amounted to agony, I collected the instruments of life around me, that I might infuse a spark of being into the lifeless thing that lay at my feet.

It was already one in the morning; the rain pattered dismally against the panes, and my candle was nearly burnt out, when, by the glimmer of the half-extinguished light, I saw the dull yellow eye of the creature open; it breathed hard, and a convulsive motion agitated its limbs.

How can I describe my emotions at this catastrophe, or how delineate the wretch whom with such infinite pains and care I had endeavoured to form? His limbs were in proportion, and I had selected his features as beautiful. Beautiful! Great God! His yellow skin scarcely covered the work of muscles and arteries beneath; his hair was of a lustrous black, and flowing; his teeth of a pearly whiteness; but these luxuriances only formed a more horrid contrast with his watery eyes, that seemed almost of the same colour as the dun-white sockets in which they were set, his shrivelled complexion and straight black lips.

The beauty of the dream vanished, and breathless horror and disgust filled my heart. Unable to endure the aspect of the being I had created, I rushed out of the room.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Victor Frankenstein's obsessive pursuit of scientific glory blinds him to the ethical consequences of his creation.",
                        "The immediate abandonment of the Creature highlights the tragic failure of parental and creator responsibility.",
                        "The romantic contrast between aesthetic aspiration and horrifying physical reality."
                    ),
                    studyQuestions = listOf(
                        "Why does Mary Shelley subtitle the novel 'The Modern Prometheus'?",
                        "How does Victor's reaction immediately upon the Creature awakening set the stage for subsequent tragedy?"
                    )
                )
            )
        ),

        Book(
            id = "short_history_world",
            title = "A Short History of the World",
            author = "H.G. Wells",
            category = BookCategory.HISTORY,
            year = "1922",
            pageCount = 410,
            rating = 4.7f,
            description = "A grand sweep of human history, from the origins of planetary life and early river valley civilizations to world wars and modern societies.",
            coverGradientStart = Color(0xFFB45309),
            coverGradientEnd = Color(0xFF78350F),
            chapters = listOf(
                BookChapter(
                    number = 1,
                    title = "The First River Civilizations (Nile & Mesopotamia)",
                    readTimeMinutes = 8,
                    content = """
Civilization did not begin in a single flash, but grew slowly along the great alluvial river valleys where agriculture, water irrigation, and seasonal floods demanded coordinated human organization.

In the fertile crescent between the Tigris and Euphrates rivers, the Sumerians built the first city-states—Ur, Uruk, and Lagash. They developed cuneiform script on clay tablets to record storehouse grain, trade contracts, and religious hymns. In doing so, humanity invented writing, transitioning from prehistoric oral tradition to documented history.

Simultaneously along the Nile, annual inundations replenished fertile soil, enabling predictable surpluses under unified pharaonic rule. The architectural engineering of the Pyramids at Giza and monumental temple complexes demonstrated advanced geometry, astronomy, and administrative coordination.

Common factors that enabled early civilization:
1. Surplus Food Production: Farmers produced more than individual households consumed, enabling division of labor (blacksmiths, scribes, engineers, scholars).
2. Record Keeping: Bureaucracy required mathematics, calendars, and writing systems.
3. Centralized Law: The Code of Hammurabi established explicit statutory standards ('An eye for an eye') replacing subjective tribal retribution.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Agricultural surplus is the prerequisite for division of labor and urbanization.",
                        "Writing originated primarily as a bureaucratic and accounting innovation before turning to literature.",
                        "Written law codes established uniform legal expectations across diverse populations."
                    ),
                    studyQuestions = listOf(
                        "Why were river floodplains uniquely suited for early human urbanization?",
                        "What was the social significance of the Code of Hammurabi?"
                    )
                )
            )
        )
    )

    fun getById(id: String): Book? = books.find { it.id == id }

    fun filter(query: String, category: BookCategory): List<Book> {
        return books.filter { book ->
            val matchesCategory = category == BookCategory.ALL || book.category == category
            val matchesQuery = query.isBlank() ||
                book.title.contains(query, ignoreCase = true) ||
                book.author.contains(query, ignoreCase = true) ||
                book.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }
}
