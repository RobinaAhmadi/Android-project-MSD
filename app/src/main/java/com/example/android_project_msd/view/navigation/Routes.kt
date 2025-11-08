package com.example.android_project_msd.view.navigation

object Routes {
    const val FrontPage = "frontpage"
    const val Login = "login"
    const val Home = "home"
    const val Groups = "groups"
    const val CreateGroup = "create_group"
    const val CreateProfile = "create_profile"
    const val Profile = "profile"

    const val GroupDetailArg = "groupId"
    const val GroupDetail = "group/{$GroupDetailArg}"
    fun groupDetail(id: String) = "group/$id"
}
