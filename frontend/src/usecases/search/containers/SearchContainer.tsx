import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {RootState} from '../../../reducers/index';
import {PageContainer} from '../../common/components/layouts/layout/PageLayout';
import {SearchContentBox} from '../components/SearchContentBox';
import {SearchParameter} from '../models/searchModels';
import {selectSearchOption} from '../searchActions';
import {SearchState} from '../searchReducer';
import {SearchOptionsLoaderContainer} from './SearchOptionsLoaderContainer';

export interface SearchStateToProps {
  search: SearchState;
}

export interface SearchDispatchToProps {
  selectSearchOption: (searchParameters: SearchParameter) => void;
}

export const SearchContainerComponent = (props: SearchStateToProps & SearchDispatchToProps & InjectedAuthRouterProps) =>
  (
    <PageContainer>
      <SearchOptionsLoaderContainer>
        <SearchContentBox
          search={props.search}
          selectSearchOption={props.selectSearchOption}
        />
      </SearchOptionsLoaderContainer>
    </PageContainer>
  );

const mapStateToProps = ({search}: RootState): SearchStateToProps => {
  return {
    search,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  selectSearchOption,
}, dispatch);

export const SearchContainer =
  connect<SearchStateToProps, SearchDispatchToProps, {}>
  (mapStateToProps, mapDispatchToProps)(SearchContainerComponent);
