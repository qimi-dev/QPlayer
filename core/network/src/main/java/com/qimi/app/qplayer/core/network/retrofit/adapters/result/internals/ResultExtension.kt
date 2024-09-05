/*
 * Copyright (C) 2022 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("UNCHECKED_CAST")

package com.qimi.app.qplayer.core.network.retrofit.adapters.result.internals

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Invocation
import retrofit2.Response
import java.lang.reflect.Type

private val responseParser: Gson = GsonBuilder()
    .disableHtmlEscaping()
    .setLenient()
    .create()

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.0
 *
 * Returns [Result] from the [Response] instance and [Call] interface.
 */
internal fun <T> Response<T>.toResult(call: Call<T>, paramType: Type): Result<T> {
    return kotlin.runCatching {
        if (isSuccessful) {
            body() ?: emptyBodyHandle(call, paramType)
        } else {
            // TODO 服务器响应失败，构建失败信息
            throw HttpException(this)
        }
    }
}

/**
 * You can confine the response type to Unit if you need to handle empty body, e.g, 204 No content.
 */
internal fun <T> Response<T>.emptyBodyHandle(call: Call<T>, paramType: Type): T {
    return if (paramType == Unit::class.java) {
        Unit as T
    } else {
        val invocation = call.request().tag(Invocation::class.java)!!
        val method = invocation.method()
        throw KotlinNullPointerException(
            "Response from " +
                    method.declaringClass.name +
                    '.' +
                    method.name +
                    " was null but response body type was declared as non-null"
        )
    }
}

