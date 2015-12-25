# Add malata M1 Apps and Services
# cqf add MOPLUES-2 20151006

CUR_PATH := vendor/malatamobile
PRODUCT_PACKAGES += \
    FactoryTest


glib_files := $(shell ls $(CUR_PATH)/lib )
PRODUCT_COPY_FILES += $(foreach file, $(glib_files), \
$(CUR_PATH)/lib/$(file):system/lib/$(file))

glib64_files := $(shell ls $(CUR_PATH)/lib64 )
PRODUCT_COPY_FILES += $(foreach file, $(glib64_files), \
$(CUR_PATH)/lib64/$(file):system/lib64/$(file))

gbin_files := $(shell ls $(CUR_PATH)/bin )
PRODUCT_COPY_FILES += $(foreach file, $(gbin_files), \
$(CUR_PATH)/bin/$(file):system/bin/$(file))
