import {default as classNames} from 'classnames';
import ActionSearch from 'material-ui/svg-icons/action/search';
import * as React from 'react';
import {colors} from '../../app/colors';
import {GlobalSearchProps} from '../../containers/GlobalSearchContainer';
import {firstUpperTranslated} from '../../services/translationService';
import {Row, RowMiddle} from '../layouts/row/Row';
import './GlobalSearch.scss';
import {useGlobalSearch} from './globalSearchHook';
import SvgIconProps = __MaterialUI.SvgIconProps;

const style: React.CSSProperties = {
  color: colors.lightBlack,
  cursor: 'pointer',
  position: 'absolute',
  left: 16,
  top: 9,
  width: 24,
};

const iconProps: SvgIconProps = {
  style,
  hoverColor: colors.iconHover,
};

export const GlobalSearch = (props: GlobalSearchProps) => {
  const {value, onChange, onEnter} = useGlobalSearch(props);

  return (
    <RowMiddle className="GlobalSearch-Container">
      <Row className={classNames('GlobalSearch', {hasValue: !!value})}>
        <input
          type="textfield"
          className="GlobalSearch-input"
          value={value && decodeURIComponent(value)}
          onChange={onChange}
          onKeyPress={onEnter}
          placeholder={firstUpperTranslated('find meters')}
        />
        <ActionSearch {...iconProps}/>
      </Row>
    </RowMiddle>
  );
};
