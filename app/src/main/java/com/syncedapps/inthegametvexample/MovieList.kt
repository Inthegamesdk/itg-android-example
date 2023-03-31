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
            "https://media.inthegame.io/uploads/videos/streamers/e5ba6651e74cd163d9159b4284581c54.6ba8db5ef482491e79b7371146a413b8.mp4"
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
