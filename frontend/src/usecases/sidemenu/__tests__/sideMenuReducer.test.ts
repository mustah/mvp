import {toggleShowHideSideMenu} from '../sideMenuActions';
import {sideMenu} from '../sideMenuReducer';

describe('sideMenuReducer', () => {

  it('side menu is initially opened ', () => {
    expect(sideMenu(undefined, toggleShowHideSideMenu())).toEqual({isOpen: false});
  });

  it('toggles the state', () => {
    expect(sideMenu({isOpen: true}, toggleShowHideSideMenu())).toEqual({isOpen: false});
    expect(sideMenu({isOpen: false}, toggleShowHideSideMenu())).toEqual({isOpen: true});
  });
});
