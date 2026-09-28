//= require ShellMenu
//= require ShellNavbar
//= require ShellUserMenu

class Shell extends Page {

    static initialize() {
        PageStickyBox.setTop($('#shell-navbar'));
    }

    static finalize() {
        Transition.wsConnect();
    }

    static onContentChange() {
        ShellMenu.updateActiveItem();
        ShellMenu.prepareInitialNavigation();
        ShellMenu.focusContentAfterNavigation();

        let $navbar = $('#shell-navbar');
        if (PageStickyBox.isActive) {
            $navbar.addClass('has-sticky');
        } else {
            $navbar.removeClass('has-sticky');
        }
    }

}

Page.register(Shell);