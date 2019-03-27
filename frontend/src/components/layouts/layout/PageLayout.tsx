import {default as classNames} from 'classnames';
import * as React from 'react';
import {GlobalSearchContainer} from '../../../containers/GlobalSearchContainer';
import {WithChildren} from '../../../types/Types';
import {TopMenu} from '../../../usecases/topmenu/component/TopMenu';
import {LogoContainer} from '../../../usecases/topmenu/containers/LogoContainer';
import {Footer} from '../../footer/Footer';
import {StateToProps, withSideMenu} from '../../hoc/withSideMenu';
import {Column} from '../column/Column';
import './Layout.scss';

type Props = WithChildren & StateToProps;

const Content = ({children}: WithChildren) => (
  <Column className="Content">
    {children}
    <Footer/>
  </Column>
);

const PageLayoutComponent = (props: Props) => {
  const {isSideMenuOpen} = props;
  return (
    <div className={classNames('PageLayout', {isSideMenuOpen})}>
      <TopMenu>
        <LogoContainer/>
        <GlobalSearchContainer/>
      </TopMenu>
      <Content {...props}/>
    </div>
  );
};

const AdminPageLayoutComponent = (props: Props) => {
  const {isSideMenuOpen} = props;
  return (
    <div className={classNames('PageLayout', {isSideMenuOpen})}>
      <TopMenu>
        <LogoContainer/>
      </TopMenu>
      <Content {...props}/>
    </div>
  );
};

export const PageLayout = withSideMenu<WithChildren>(PageLayoutComponent);

export const AdminPageLayout = withSideMenu<WithChildren>(AdminPageLayoutComponent);
