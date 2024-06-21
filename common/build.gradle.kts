group = "org.terraform"

dependencies {
    compileOnly(group = "org.spigotmc", name = "spigot", version = "1.18-R0.1-SNAPSHOT")
    implementation("space.arim.morepaperlib:morepaperlib:0.4.2")
	//compileOnly(group = "org.spigotmc", name = "spigot", version = "1.16.4-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:20.1.0")
}
//Set this to the lowest compat in implementation
java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}