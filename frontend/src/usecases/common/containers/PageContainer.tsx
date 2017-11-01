import * as classNames from 'classnames';
import {Pathname} from 'history';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname, isSearchPage} from '../../../selectors/routerSelectors';
import {closeSearch, selectPeriod} from '../../../state/search/selection/selectionActions';
import {OnSelectPeriod} from '../../../state/search/selection/selectionModels';
import {getSelectedPeriod} from '../../../state/search/selection/selectionSelectors';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {OnClick, Period} from '../../../types/Types';
import {SelectionMenu} from '../../selection/components/selection-menu/SelectionMenu';
import {SelectionMenuSummary} from '../../selection/components/selection-menu/SelectionMenuSummary';
import {SearchMenuWrapper} from '../../selection/components/selection-menu/SelectionMenuWrapper';
import {Column} from '../components/layouts/column/Column';
import {Content} from '../components/layouts/content/Content';
import {Layout} from '../components/layouts/layout/Layout';

interface StateToProps {
  pathname: Pathname;
  isSearchPage: boolean;
  isSideMenuOpen: boolean;
  children?: React.ReactNode;
  selectedPeriod: Period;
}

interface DispatchToProps {
  closeSearch: OnClick;
  selectPeriod: OnSelectPeriod;
}

const PageContainerComponent = (props: StateToProps & DispatchToProps) => {
  const {children, closeSearch, isSearchPage, isSideMenuOpen, pathname, selectPeriod, selectedPeriod} = props;

  const renderSelectionSearch = isSearchPage
    ? <SelectionMenu onClick={closeSearch}/>
    : <SelectionMenuSummary pathname={pathname}/>;

  return (
    <Layout>
      <SearchMenuWrapper
        period={selectedPeriod}
        className={classNames({isSideMenuOpen})}
        selectPeriod={selectPeriod}
      >
        {renderSelectionSearch}
      </SearchMenuWrapper>

      <Column className="flex-1">
        <Content className="Content-main">
          {children}
        </Content>
      </Column>
    </Layout>
  );
};

const mapStateToProps = ({routing, ui, searchParameters: {selection}}: RootState): StateToProps => {
  return {
    isSearchPage: isSearchPage(routing),
    pathname: getPathname(routing),
    isSideMenuOpen: isSideMenuOpen(ui),
    selectedPeriod: getSelectedPeriod(selection),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  closeSearch,
  selectPeriod,
}, dispatch);

export const PageContainer =
  connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(PageContainerComponent);
