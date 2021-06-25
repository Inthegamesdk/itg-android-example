package com.syncedapps.inthegametvexample

object MovieList {
    val MOVIE_CATEGORY = arrayOf(
            "Demo channels")

    val list: List<Movie> by lazy {
        setupMovies()
    }
    private var count: Long = 0

    private fun setupMovies(): List<Movie> {
        val title = arrayOf(
                "Soccer demo")

        val description = "This example displays some Inthegame interactions on video content."
        val studio = arrayOf(
                "Inthegame")
        val videoUrl = arrayOf(
            "https://media.inthegame.io/uploads/videos/streamers/a64706dd0f42356e93d299075940c456.857ecbb7a131f9bb4940a6b8ad5ec70e.mp4")
        val bgImageUrl = arrayOf(
                "ball")
        val cardImageUrl = arrayOf(
                "football")

        val list = title.indices.map {
            buildMovieInfo(
                    title[it],
                    description,
                    studio[it],
                    videoUrl[it],
                    cardImageUrl[it],
                    bgImageUrl[it])
        }

        return list
    }

    private fun buildMovieInfo(
            title: String,
            description: String,
            studio: String,
            videoUrl: String,
            cardImageUrl: String,
            backgroundImageUrl: String): Movie {
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
