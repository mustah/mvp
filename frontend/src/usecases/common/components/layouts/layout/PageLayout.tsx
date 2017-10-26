import {Pathname} from 'history';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../../../reducers/rootReducer';
import {getPathname, isSearchPage} from '../../../../../selectors/routerSelector';
import {closeSearch} from '../../../../../state/search/selection/selectionActions';
import {SelectionSearch} from '../../selection-search/SelectionSearch';
import {SelectionSearchSummary} from '../../selection-search/SelectionSearchSummary';
import {Column} from '../column/Column';
import {Content} from '../content/Content';
import {Layout} from './Layout';

interface StateToProps {
  children?: React.ReactNode[] | React.ReactNode;
  pathname: Pathname;
  isSearchPage: boolean;
}

interface DispatchToProps {
  closeSearch: () => void;
}

const PageLayoutContainerComponent = (props: StateToProps & DispatchToProps) => {
  const {children, closeSearch, isSearchPage, pathname} = props;

  const renderSelectionSearch = isSearchPage
    ? <SelectionSearch close={closeSearch}/>
    : <SelectionSearchSummary pathname={pathname}/>;

  return (
    <Layout>
      <Column className="flex-1">
        {renderSelectionSearch}
        <Content>
          {children}
        </Content>
      </Column>
    </Layout>
  );
};

const mapStateToProps = (state: RootState): StateToProps => {
  return {
    isSearchPage: isSearchPage(state.routing),
    pathname: getPathname(state.routing),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  closeSearch,
}, dispatch);

export const PageContainer =
  connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(PageLayoutContainerComponent);
