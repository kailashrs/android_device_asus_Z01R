#
# Copyright (C) 2018 The LineageOS Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/product_launched_with_o.mk)

# Inherit from Z01R device
$(call inherit-product, device/asus/Z01R/device.mk)

DERP_BUILDTYPE := Official

# Inherit some common AOSiP stuff.
$(call inherit-product, vendor/aosip/config/common_full_phone.mk)
TARGET_BOOT_ANIMATION_RES := 1080

#GApps
TARGET_GAPPS_ARCH := arm64
IS_PHONE := true
TARGET_INCLUDE_STOCK_ARCORE := false
TARGET_SUPPORTS_GOOGLE_RECORDER := false

PRODUCT_BRAND := asus
PRODUCT_DEVICE := Z01R
PRODUCT_MANUFACTURER := asus
PRODUCT_MODEL := Zenfone 5Z
PRODUCT_NAME := derp_Z01R

PRODUCT_GMS_CLIENTID_BASE := android-asus
PRODUCT_ART_TARGET_INCLUDE_DEBUG_BUILD := false
PRODUCT_MINIMIZE_JAVA_DEBUG_INFO := true
PRODUCT_PACKAGES_DEBUG := false
PRODUCT_PACKAGES_DEBUG_ASAN := false

TARGET_VENDOR_PRODUCT_NAME := Z01R
TARGET_VENDOR_DEVICE_NAME := Z01R

PRODUCT_BUILD_PROP_OVERRIDES += \
    TARGET_DEVICE=Z01R \
    PRODUCT_NAME=Z01R
