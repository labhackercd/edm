package net.labhackercd.edemocracia.data;

import net.labhackercd.edemocracia.data.api.ApiModule;

import dagger.Module;

@Module(
        includes = ApiModule.class,
        library = true,
        complete = false
)
@SuppressWarnings("UnusedDeclaration")
public class DataModule {
}
