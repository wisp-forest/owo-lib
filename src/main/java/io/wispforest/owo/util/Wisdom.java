package io.wispforest.owo.util;

import com.google.common.collect.ImmutableList;
import io.wispforest.owo.Owo;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;

import java.util.List;

public final class Wisdom {

    private Wisdom() {}

    private static final RandomSource CRYSTAL_BALL = RandomSource.create();
    public static final List<String> ALL_THE_WISDOM = ImmutableList.of(
            "assume less - mald more",
            ":ctft: literally infinite and counting",
            "Unobtainium is usually found underground",
            "Did you know that Air is made of 78 percent nitrogen and 21 percent oxygen?",
            "We don't have to worry about people re-inventing Venezuela glisco, that's their problem for integrating the Bourgeoisie into their pack",
            "fuck i need to update tags again",
            "Don't use Forge, use Froge!",
            "Noaaan: I'm talking to my inner demons. Which is all of you",
            "There is a 1% chance that instead of Frog, you get Froge!",
            "it seems to react to redstone",
            "remember to update ubuntu, before it is too late",
            "DerGeistdesMatze - i will take the l",
            "idwtialsimmoedm - I didn't want to install a library so I made my own enchantment descriptions mod",
            "Here at Wisp Forest© we employ Wisp Tech Support™ magic, which solves your problem when you ask",
            "chyz: How could you do this to me, Blod. I loved you like a guy I don't know in real life :(",
            "chyzman: No sound or Ender Dragon leakage. Although Ender Dragon sound leakage is still a thing",
            "This custom packet isn't supported by Gadget - Add support via DrawPacketHandler.EVENT",
            "glisco: I like this approach, cause it's stateless. And stateless approaches are always good since state management is pain - BasiqueEvangelist: I like french bread",
            "dead people should put things in their grave, I have bills to pay - Blodgharm",
            "Maybe we take a page out of the Wisp Forest handbook: When in doubt, steal glisco's code",
            "make a man a mod, he'll be enteratined for a few minutes - teach a man to mod, and he'll mald till the end of time",
            "who invited dafuqs",
            "chyz: There's a snake in my prison pocket",
            "Snakes are inherently funny",
            "glisco makes a dollar, I make a dime, that's why I mald on company time",
            "Don't trust naschhorn, the Master of Taglocks, around you or your bed",
            "forge?",
            "as a based person once said: it doesn't have to be balanced unless you're making a pvp focused mod",
            "yes this is a tip",
            "I bet DeetHunter will never see this one!",
            "I think that's not the only reason Spectrum isn't compatible with Quilt",
            "My Seethenhagen factory makes 331 pounds of licorice per hour",
            "chyz: now i just need to painstakingly move jerry (the little tiny lil fella tiny guy man button) to correct spot when recipe book is opened",
            "chyz: bro, can you stop breathing",
            "Noaaan: It's surprising how much knowledge is lost by the simple fact that people don't know it",
            "Blodhgarm: Did you know it took four switcheroos to implement MatrixStackTransformer in the correct package?",
            "chyz: who would've thought that if you don't summon satan things go better. you can put that in wisdom if you like",
            "Its called Unobtainium until you obtain it, thats the thing",
            "i ate moss and i died",
            "Jello was 126 years old when we added this line",
            "I saw ppl complain that minor updates between big updates will ruin it for modders but I thought modders were very good and they can do what we do in a week so they should be ok with updating their mods to new versions in a day, right? :titantroll:",
            "that's a CanPickUpLoot baby zombie, the most annoying thing ever. he runs around like crazy and picks up all your shit",
            "blod: I think I need to take a book out of your page",
            "blod: he her"
    );

    public static void spread() {
        Owo.LOGGER.info(Util.getRandom(ALL_THE_WISDOM, CRYSTAL_BALL));
    }

}
