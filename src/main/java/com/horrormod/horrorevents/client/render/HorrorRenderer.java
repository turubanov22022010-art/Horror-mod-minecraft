package com.horrormod.horrorevents.client.render;

import com.horrormod.horrorevents.HorrorEvents;
import com.horrormod.horrorevents.entity.HorrorMob;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Универсальный рендерер: geo/entity/<name>.geo.json, textures/entity/<name>.png, animations/entity/<name>.animation.json. */
public class HorrorRenderer<T extends HorrorMob> extends GeoEntityRenderer<T> {
    public HorrorRenderer(EntityRendererProvider.Context ctx, String name) {
        super(ctx, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(HorrorEvents.MOD_ID, name), true));
    }
}
