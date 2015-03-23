package net.labhackercd.edemocracia.service;

import dagger.Module;

@Module(complete = false, library = true, injects = {
        AddMessageTask.class,
        AddMessageService.class
})
@SuppressWarnings("UnusedDeclaration")
public class ServiceModule {
    // Nothing here.
}
