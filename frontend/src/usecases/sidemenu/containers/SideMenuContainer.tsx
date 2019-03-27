import {withSideMenu} from '../../../components/hoc/withSideMenu';
import {ClassNamed, WithChildren} from '../../../types/Types';
import {SideMenu} from '../components/SideMenu';

export const SideMenuContainer = withSideMenu<WithChildren & ClassNamed>(SideMenu);
