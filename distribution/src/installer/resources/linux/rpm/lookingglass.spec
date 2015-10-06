Summary: %{app_title}
Name: %{app_file}
Version: %{app_version}
Release: 1
Group: Applications/Education
Packager: %{app_vendor}
URL: %{app_homepage}
Requires: java >= 1:1.8.0
Requires: ffmpeg >= 0.10
Requires: vlc >= 2.1.0
Requires: zip >= 3.0
License: Looking Glass License and Looking Glass Art Gallery License and Alice 3 License
Source: %{app_file}-%{app_version}.tar.gz

%description
%{app_description}

%prep
%setup -q

%build

%install
[ -n "$RPM_BUILD_ROOT" -a "$RPM_BUILD_ROOT" != / ] && rm -rf "$RPM_BUILD_ROOT"

mkdir -p "$RPM_BUILD_ROOT%{_datadir}"
cp -r "$RPM_BUILD_DIR/%{app_file}-%{app_version}/%{app_file}" "$RPM_BUILD_ROOT%{_datadir}/%{app_file}"

mkdir -p "$RPM_BUILD_ROOT%{_bindir}"
sed -e "s^base_dir=.*$^base_dir="%{_datadir}"/%{app_file}^g" < "$RPM_BUILD_DIR/%{app_file}-%{app_version}/%{app_file}/%{app_file}" > "$RPM_BUILD_ROOT%{_bindir}/%{app_file}"

mkdir -p "$RPM_BUILD_ROOT%{_datadir}"
cp -r "$RPM_BUILD_DIR/%{app_file}-%{app_version}/linux/icons" "$RPM_BUILD_ROOT%{_datadir}/."

mkdir -p "$RPM_BUILD_ROOT%{_datadir}/applications"
cp -r "$RPM_BUILD_DIR/%{app_file}-%{app_version}/linux/lookingglass.desktop" "$RPM_BUILD_ROOT%{_datadir}/applications/."

mkdir -p "$RPM_BUILD_ROOT%{_datadir}/mime/packages"
cp -r "$RPM_BUILD_DIR/%{app_file}-%{app_version}/linux/lookingglass.xml" "$RPM_BUILD_ROOT%{_datadir}/mime/packages/."

%clean
[ -n "$RPM_BUILD_ROOT" -a "$RPM_BUILD_ROOT" != / ] && rm -rf "$RPM_BUILD_ROOT"

%files
# Looking Glass resources
%attr(755, -, -) %{_bindir}/%{app_file}
%attr(755, -, -) %{_datadir}/%{app_file}/jre/bin/java
%{_datadir}/%{app_file}/*

# Desktop integration
%{_datadir}/applications/*
%{_datadir}/mime/packages/*

# Desktop icons
%{_datadir}/icons/hicolor/16x16/mimetypes/application-x-lookingglass-project.png
%{_datadir}/icons/hicolor/16x16/apps/lookingglass.png
%{_datadir}/icons/hicolor/22x22/mimetypes/application-x-lookingglass-project.png
%{_datadir}/icons/hicolor/22x22/apps/lookingglass.png
%{_datadir}/icons/hicolor/24x24/mimetypes/application-x-lookingglass-project.png
%{_datadir}/icons/hicolor/24x24/apps/lookingglass.png
%{_datadir}/icons/hicolor/32x32/mimetypes/application-x-lookingglass-project.png
%{_datadir}/icons/hicolor/32x32/apps/lookingglass.png
%{_datadir}/icons/hicolor/48x48/mimetypes/application-x-lookingglass-project.png
%{_datadir}/icons/hicolor/48x48/apps/lookingglass.png
%{_datadir}/icons/hicolor/256x256/mimetypes/application-x-lookingglass-project.png
%{_datadir}/icons/hicolor/256x256/apps/lookingglass.png
%{_datadir}/icons/hicolor/scalable/mimetypes/application-x-lookingglass-project.svg
%{_datadir}/icons/hicolor/scalable/apps/lookingglass.svg

%post
update-desktop-database &> /dev/null || :
update-mime-database %{_datadir}/mime &> /dev/null || :
touch --no-create %{_datadir}/icons/hicolor &>/dev/null || :

%postun
update-desktop-database &> /dev/null || :
update-mime-database %{_datadir}/mime &> /dev/null || :
if [ $1 -eq 0 ] ; then
    touch --no-create %{_datadir}/icons/hicolor &>/dev/null
    gtk-update-icon-cache %{_datadir}/icons/hicolor &>/dev/null || :
fi

%posttrans
gtk-update-icon-cache %{_datadir}/icons/hicolor &>/dev/null || :

