import {default as classNames} from 'classnames';
import ActionSearch from 'material-ui/svg-icons/action/search';
import * as React from 'react';
import {DispatchToProps} from '../../containers/GlobalSearchContainer';
import {firstUpperTranslated} from '../../services/translationService';
import {Query} from '../../state/search/searchModels';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import {Row, RowMiddle} from '../layouts/row/Row';
import './GlobalSearch.scss';
import {GlobalSearch as GlobalSearchProps, useGlobalSearch} from './globalSearchHook';

const iconStyle: React.CSSProperties = {
  cursor: 'pointer',
  position: 'absolute',
  left: 16,
  top: 9,
  width: 24,
};

const Input = withCssStyles(({onChange, onEnter, value, cssStyles: {primary}}: GlobalSearchProps & ThemeContext) => (
  <input
    type="textfield"
    className="GlobalSearch-input"
    value={value && decodeURIComponent(value)}
    onChange={onChange}
    onKeyPress={onEnter}
    placeholder={firstUpperTranslated('find meters')}
    style={{backgroundColor: primary.bgDarkest, color: primary.fg}}
  />
));

const SearchIcon = withCssStyles(({cssStyles: {primary}}: ThemeContext) => (
  <ActionSearch style={{...iconStyle, color: primary.fg}} hoverColor={primary.fgHover}/>
));

export type Props = Query & DispatchToProps;

export const GlobalSearch = (props: Props) => {
  const searchProps = useGlobalSearch(props);
  return (
    <RowMiddle className="GlobalSearch-Container">
      <Row className={classNames('GlobalSearch', {hasValue: !!searchProps.value})}>
        <Input  {...searchProps}/>
        <SearchIcon/>
      </Row>
    </RowMiddle>
  );
};
