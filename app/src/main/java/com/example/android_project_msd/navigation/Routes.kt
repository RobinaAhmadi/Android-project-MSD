package com.example.android_project_msd.navigation

object Routes {
    const val FrontPage = "frontpage"
    const val Login = "login"
    const val Groups = "groups"
    const val CreateProfile = "create_profile"
    const val Profile = "profile"

    const val GroupDetailArg = "groupId"
    const val GroupDetail = "group/{$GroupDetailArg}"
    fun groupDetail(id: String) = "group/$id"
}

