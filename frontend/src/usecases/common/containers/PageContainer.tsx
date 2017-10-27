import * as classNames from 'classnames';
import {Pathname} from 'history';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname, isSearchPage} from '../../../selectors/routerSelectors';
import {closeSearch} from '../../../state/search/selection/selectionActions';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {Column} from '../components/layouts/column/Column';
import {Content} from '../components/layouts/content/Content';
import {Layout} from '../components/layouts/layout/Layout';
import {SelectionSearch} from '../components/selection-search/SelectionSearch';
import {SelectionSearchSummary} from '../components/selection-search/SelectionSearchSummary';

interface StateToProps {
  pathname: Pathname;
  isSearchPage: boolean;
  isSideMenuOpen: boolean;
  children?: React.ReactNode;
}

interface DispatchToProps {
  closeSearch: () => void;
}

const PageContainerComponent = (props: StateToProps & DispatchToProps) => {
  const {children, closeSearch, isSearchPage, isSideMenuOpen, pathname} = props;

  const renderSelectionSearch = isSearchPage
    ? <SelectionSearch close={closeSearch} className={classNames({isSideMenuOpen})}/>
    : <SelectionSearchSummary pathname={pathname} className={classNames({isSideMenuOpen})}/>;

  return (
    <Layout>
      {renderSelectionSearch}
      <Column className="flex-1">
        <Content className="Content-main">
          {children}
        </Content>
      </Column>
    </Layout>
  );
};

const mapStateToProps = ({routing, ui}: RootState): StateToProps => {
  return {
    isSearchPage: isSearchPage(routing),
    pathname: getPathname(routing),
    isSideMenuOpen: isSideMenuOpen(ui),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  closeSearch,
}, dispatch);

export const PageContainer =
  connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(PageContainerComponent);
