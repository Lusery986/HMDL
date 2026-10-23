/*
 * Hu Mai Diao! Launcher
 * Copyright (C) 2025 huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.jackhuang.hmcl.util.i18n;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.jackhuang.hmcl.util.i18n.translator.Translator;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.io.IOException;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// The launcher UI language.
///
/// This build ships a single UI language (Simplified Chinese), so there is exactly one
/// instance, [SupportedLocale#DEFAULT], and every lookup resolves to it.
@NotNullByDefault
@JsonAdapter(SupportedLocale.TypeAdapter.class)
public final class SupportedLocale {
    /// The only supported launcher UI locale.
    public static final SupportedLocale DEFAULT = new SupportedLocale();

    /// The locale backing the launcher UI.
    public static final Locale LOCALE = Locale.forLanguageTag("zh-Hans-CN");

    /// Returns the supported launcher UI locales.
    ///
    /// @return a list containing only [SupportedLocale#DEFAULT]
    public static List<SupportedLocale> getSupportedLocales() {
        return List.of(DEFAULT);
    }

    /// Resolves a locale to the launcher UI locale.
    ///
    /// @param locale the requested locale, ignored
    /// @return always [SupportedLocale#DEFAULT]
    public static SupportedLocale getLocale(Locale locale) {
        return DEFAULT;
    }

    /// Parses a locale name persisted by older launcher versions.
    ///
    /// @param name the persisted locale name, may be `null`
    /// @return always [SupportedLocale#DEFAULT]
    public static SupportedLocale getLocaleByName(@Nullable String name) {
        return DEFAULT;
    }

    private @Nullable ResourceBundle resourceBundle;
    private @Nullable List<Locale> candidateLocales;
    private @Nullable Translator translator;

    private SupportedLocale() {
    }

    /// Reports whether this is the default (and only) locale.
    ///
    /// @return always `true`
    public boolean isDefault() {
        return true;
    }

    /// Returns the identifier persisted in configuration files.
    ///
    /// @return the literal `"default"`
    public String getName() {
        return "default";
    }

    /// Returns the locale used by resource bundle lookups.
    ///
    /// @return the Chinese locale
    public Locale getLocale() {
        return LOCALE;
    }

    /// Returns the locale used when displaying this locale to the user.
    ///
    /// @return the Chinese locale
    public Locale getDisplayLocale() {
        return LOCALE;
    }

    /// Returns the locale fallback chain used to load bundled resources.
    ///
    /// @return the fallback chain for [#getDisplayLocale()]
    public List<Locale> getCandidateLocales() {
        List<Locale> locales = candidateLocales;
        if (locales == null) {
            candidateLocales = locales = LocaleUtils.getCandidateLocales(LOCALE);
        }
        return locales;
    }

    /// Returns the fontconfig match pattern used to pick a suitable CJK font.
    ///
    /// @return a fontconfig language and charset selector for Simplified Chinese
    public String getFcMatchPattern() {
        return ":lang=zh-CN:charset=0x6e38,0x620f";
    }

    /// Returns the text translator for this locale.
    ///
    /// @return the base translator
    public Translator getTranslator() {
        Translator result = translator;
        if (result == null) {
            translator = result = new Translator(this);
        }
        return result;
    }

    /// Returns whether the other locale shares the same root language.
    ///
    /// @param other the locale to compare with
    /// @return always `true`, since Chinese is the only supported language
    public boolean isSameLanguage(SupportedLocale other) {
        return true;
    }

    /// Formats a localized message.
    ///
    /// @param key the resource bundle key
    /// @param formatArgs arguments applied with [String#format]
    /// @return the formatted message, or the key when missing or malformed
    public String i18n(@PropertyKey(resourceBundle = "assets.lang.I18N") String key, Object... formatArgs) {
        try {
            return String.format(getResourceBundle().getString(key), formatArgs);
        } catch (MissingResourceException e) {
            LOG.error("Cannot find key " + key + " in resource bundle", e);
        } catch (IllegalFormatException e) {
            LOG.error("Illegal format string, key=" + key + ", args=" + Arrays.toString(formatArgs), e);
        }

        return key + Arrays.toString(formatArgs);
    }

    /// Looks up a localized message without formatting.
    ///
    /// @param key the resource bundle key
    /// @return the message, or the key when missing
    public String i18n(@PropertyKey(resourceBundle = "assets.lang.I18N") String key) {
        try {
            return getResourceBundle().getString(key);
        } catch (MissingResourceException e) {
            LOG.error("Cannot find key " + key + " in resource bundle", e);
            return key;
        }
    }

    /// Returns the resource bundle backing this locale.
    ///
    /// @return the cached Simplified Chinese resource bundle
    public ResourceBundle getResourceBundle() {
        ResourceBundle bundle = resourceBundle;
        if (bundle == null) {
            resourceBundle = bundle = ResourceBundle.getBundle("assets.lang.I18N", LOCALE,
                    DefaultResourceBundleControl.INSTANCE);
        }
        return bundle;
    }

    /// Serializes persisted locales.
    ///
    /// Older configurations may contain any language tag; reading one always yields
    /// [SupportedLocale#DEFAULT], and writing always produces `"default"`.
    public static final class TypeAdapter extends com.google.gson.TypeAdapter<SupportedLocale> {
        @Override
        public void write(JsonWriter out, @Nullable SupportedLocale value) throws IOException {
            out.value(value != null ? value.getName() : "default");
        }

        @Override
        public SupportedLocale read(JsonReader in) throws IOException {
            if (in.peek() != JsonToken.NULL) {
                in.nextString();
            }
            return DEFAULT;
        }
    }
}
