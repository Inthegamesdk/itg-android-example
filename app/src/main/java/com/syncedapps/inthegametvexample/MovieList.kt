@file:Suppress("SameParameterValue")

package com.syncedapps.inthegametvexample

object MovieList {
    val MOVIE_CATEGORY = arrayOf(
        "Demo channels"
    )

    val list: List<Movie> by lazy {
        setupMovies()
    }
    private var count: Long = 0

    private fun setupMovies(): List<Movie> {
        val title = arrayOf(
            "Soccer demo"
        )

        val description = "This example displays some Inthegame interactions on video content."
        val studio = arrayOf(
            "Inthegame"
        )
        val videoUrl = arrayOf(
            "https://media2.inthegame.io/uploads/videos/streamers/278dee276f8d43d11dad3030d0aa449e.a4ef1c02ad73f7b5ed0a6df3809abf12.mp4"
        )
        val bgImageUrl = arrayOf(
            "ball"
        )
        val cardImageUrl = arrayOf(
            "football"
        )

        val list = title.indices.map {
            buildMovieInfo(
                title[it],
                description,
                studio[it],
                videoUrl[it],
                cardImageUrl[it],
                bgImageUrl[it]
            )
        }

        return list
    }

    private fun buildMovieInfo(
        title: String,
        description: String,
        studio: String,
        videoUrl: String,
        cardImageUrl: String,
        backgroundImageUrl: String
    ): Movie {
        val movie = Movie()
        movie.id = count++
        movie.title = title
        movie.description = description
        movie.studio = studio
        movie.cardImageUrl = cardImageUrl
        movie.backgroundImageUrl = backgroundImageUrl
        movie.videoUrl = videoUrl
        return movie
    }
}
