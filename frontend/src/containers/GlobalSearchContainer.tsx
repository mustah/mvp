import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {GlobalSearch} from '../components/search-box/GlobalSearch';
import {RootState} from '../reducers/rootReducer';
import {clearValidationSearch, validationSearch} from '../state/search/searchActions';
import {OnSearch, Query} from '../state/search/searchModels';
import {OnClickWith} from '../types/Types';

interface DispatchToProps {
  onSearch: OnSearch;
  onClear: OnClickWith<string>;
}

export type GlobalSearchProps = Query & DispatchToProps;

const mapStateToProps = ({search: {validation: {query}}}: RootState): Query => ({query});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  onClear: clearValidationSearch,
  onSearch: validationSearch,
}, dispatch);

export const GlobalSearchContainer =
  connect<Query, DispatchToProps>(mapStateToProps, mapDispatchToProps)(GlobalSearch);
