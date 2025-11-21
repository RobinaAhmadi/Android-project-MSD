package com.example.android_project_msd.navigation

object Routes {
    const val FrontPage = "frontpage"
    const val Login = "login"
    const val Home = "home"
    const val Groups = "groups"
    const val CreateGroup = "create_group"
    const val CreateProfile = "create_profile"
    const val Profile = "profile"

    // Routing for group details screen
    const val GroupDetailArg = "groupId"
    const val GroupDetail = "group/{$GroupDetailArg}"
    fun groupDetail(id: String) = "group/$id"

    //Routing for group settings screen
    const val GroupSettingsArg = "groupId"
    const val GroupSettings = "group/{$GroupSettingsArg}/settings"
    fun groupSettings(id: String) = "group/$id/settings"


    // Debug-route for notification (temporary)
    const val NotificationsDebug = "notifications_debug"

    const val PaymentMethods = "payment_methods"

}
