import * as classNames from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {withRouter} from 'react-router';
import {RootState} from '../../reducers/rootReducer';
import {isSideMenuOpen} from '../../state/ui/uiSelectors';
import {Layout} from '../common/components/layouts/layout/Layout';
import {Row} from '../common/components/layouts/row/Row';
import {MainMenuContainer} from '../main-menu/containers/MainMenuContainer';
import {SideMenuContainer} from '../sidemenu/containers/SideMenuContainer';
import './_common.scss';
import './App.scss';
import {Pages} from './Pages';

interface StateToProps {
  isAuthenticated: boolean;
  isSideMenuOpen: boolean;
}

/**
 * The Application root component should extend React.Component in order
 * for HMR (hot module reloading) to work properly. Otherwise, prefer
 * functional components.
 */
class App extends React.Component<StateToProps> {

  render() {
    const {isAuthenticated, isSideMenuOpen} = this.props;

    return (
      <Row className="App">
        <MainMenuContainer/>

        <Layout className={classNames('SideMenuContainer', {isSideMenuOpen})} hide={!isAuthenticated}>
          <SideMenuContainer/>
        </Layout>

        <Pages/>
      </Row>
    );
  }
}

const mapStateToProps = ({auth, ui}: RootState): StateToProps => {
  return {
    isAuthenticated: auth.isAuthenticated,
    isSideMenuOpen: isSideMenuOpen(ui),
  };
};

export default withRouter(connect(mapStateToProps, {})(App));
