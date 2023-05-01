Pod::Spec.new do |spec|
    spec.name                     = 'AAMultiplatformLib'
    spec.version                  = '1.2.1'
    spec.homepage                 = 'https://github.com/adadaptedinc/AAMultiplatformLib'
    spec.source                   = { :git => "https://github.com/adadaptedinc/AAMultiplatformLib.git", :tag => "#{spec.version}" }
    spec.authors                  = { "Brett Clifton" => "bclifton@adadapted.com", "Matthew Kruk" => "mkruk@adadapted.com" }
    spec.license                  = { :type => "MIT", :file => "LICENSE" }
    spec.summary                  = 'Official AdAdapted iOS Swift SDK'
    spec.description              = <<-DESC
      This SDK allows you to utilize AdAdapted's service platform for displaying ads, keyword intercepts, tracking events, and more.
                       DESC

    spec.vendored_frameworks      = "AAMultiplatformLib.xcframework"
    spec.pod_target_xcconfig      = { 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64' }
    spec.libraries                = "c++"

    spec.platform                 = :ios
    spec.ios.deployment_target    = "14.1"
    spec.swift_version            = "5.2"
end