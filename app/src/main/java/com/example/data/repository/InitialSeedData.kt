package com.example.data.repository

import com.example.data.local.ArticleEntity

object InitialSeedData {
    fun getPreloadedArticles(): List<ArticleEntity> = listOf(
        ArticleEntity(
            id = 1,
            title = "Architecting Resilient Offline-First Mobile Architectures",
            body = "Building truly modern mobile applications requires treating network unavailability as a first-class state. By pairing asynchronous remote payload fetchers with local SQLite persistence layers, applications deliver instantaneous response times and seamless UX even in dead-zones.",
            tags = "architecture,mobile,offline,cloud",
            likes = 342,
            dislikes = 12,
            views = 1250,
            userId = 101,
            authorName = "Elena Vance",
            readTimeMinutes = 4,
            category = "Engineering",
            isBookmarked = true,
            cachedAtTimestamp = System.currentTimeMillis() - 3600000,
            rawJsonPayload = "{\"id\":1,\"title\":\"Architecting Resilient Offline-First Mobile Architectures\",\"category\":\"Engineering\",\"tags\":[\"architecture\",\"mobile\",\"offline\",\"cloud\"]}"
        ),
        ArticleEntity(
            id = 2,
            title = "Reactive State Machines: Unidirectional Data Flow in Practice",
            body = "Unidirectional Data Flow architectures like Redux and BLoC enforce deterministic state transitions. All user intents flow as discrete Actions through a centralized Store, ensuring that UI renderings remain pure reflections of state.",
            tags = "state,bloc,redux,kotlin",
            likes = 289,
            dislikes = 8,
            views = 980,
            userId = 102,
            authorName = "Marcus Brody",
            readTimeMinutes = 6,
            category = "Architecture",
            isBookmarked = false,
            cachedAtTimestamp = System.currentTimeMillis() - 7200000,
            rawJsonPayload = "{\"id\":2,\"title\":\"Reactive State Machines: Unidirectional Data Flow in Practice\",\"category\":\"Architecture\",\"tags\":[\"state\",\"bloc\",\"redux\"]}"
        ),
        ArticleEntity(
            id = 3,
            title = "Decentralized Cache Synchronization Strategies",
            body = "Optimistic UI mutations combined with background exponential backoff retry policies prevent user disruption during intermittent network drops. When connectivity resumes, local write queues reconcile cleanly with remote endpoints.",
            tags = "sync,distributed,database,network",
            likes = 412,
            dislikes = 15,
            views = 1840,
            userId = 103,
            authorName = "Dr. Aris Thorne",
            readTimeMinutes = 5,
            category = "Cloud & Data",
            isBookmarked = false,
            cachedAtTimestamp = System.currentTimeMillis() - 10800000,
            rawJsonPayload = "{\"id\":3,\"title\":\"Decentralized Cache Synchronization Strategies\",\"category\":\"Cloud & Data\",\"tags\":[\"sync\",\"distributed\"]}"
        ),
        ArticleEntity(
            id = 4,
            title = "Design Systems at Scale: Fluid Micro-Interactions",
            body = "Polished design systems go far beyond tokenized palettes. Consistent touch feedback, spring physics animations, and strict minimum interactive target boundaries (48dp) turn utilitarian layouts into expressive experiences.",
            tags = "design,ui,material,ux",
            likes = 520,
            dislikes = 6,
            views = 2410,
            userId = 104,
            authorName = "Clara Zhang",
            readTimeMinutes = 3,
            category = "Design",
            isBookmarked = false,
            cachedAtTimestamp = System.currentTimeMillis() - 14400000,
            rawJsonPayload = "{\"id\":4,\"title\":\"Design Systems at Scale: Fluid Micro-Interactions\",\"category\":\"Design\",\"tags\":[\"design\",\"ui\",\"material\"]}"
        ),
        ArticleEntity(
            id = 5,
            title = "Low-Latency Remote Payload Deserialization with Kotlin",
            body = "High throughput JSON parsing libraries such as Moshi with reflection-less code generation significantly decrease memory allocation spikes and CPU cycles on mobile devices processing large array payloads.",
            tags = "kotlin,moshi,performance,network",
            likes = 198,
            dislikes = 4,
            views = 720,
            userId = 105,
            authorName = "Devon Sanders",
            readTimeMinutes = 5,
            category = "Engineering",
            isBookmarked = false,
            cachedAtTimestamp = System.currentTimeMillis() - 18000000,
            rawJsonPayload = "{\"id\":5,\"title\":\"Low-Latency Remote Payload Deserialization with Kotlin\",\"category\":\"Engineering\",\"tags\":[\"kotlin\",\"moshi\"]}"
        ),
        ArticleEntity(
            id = 6,
            title = "Modern Security Boundaries for Cloud-Connected Edge Clients",
            body = "Handling credentials securely, preventing token leakage in local storage, and configuring robust Transport Layer Security (TLS) certificate pinning are foundational safeguards for modern data platforms.",
            tags = "security,cloud,privacy,api",
            likes = 310,
            dislikes = 9,
            views = 1120,
            userId = 106,
            authorName = "Siddharth Rao",
            readTimeMinutes = 7,
            category = "Security",
            isBookmarked = false,
            cachedAtTimestamp = System.currentTimeMillis() - 21600000,
            rawJsonPayload = "{\"id\":6,\"title\":\"Modern Security Boundaries for Cloud-Connected Edge Clients\",\"category\":\"Security\",\"tags\":[\"security\",\"cloud\"]}"
        )
    )
}
