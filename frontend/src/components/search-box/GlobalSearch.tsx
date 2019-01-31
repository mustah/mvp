import {default as classNames} from 'classnames';
import ActionSearch from 'material-ui/svg-icons/action/search';
import ContentClear from 'material-ui/svg-icons/content/clear';
import * as React from 'react';
import {colors} from '../../app/themes';
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
  const {value, onChange, onEnter, onClearValue} = useGlobalSearch(props);

  return (
    <RowMiddle className="GlobalSearch-Container">
      <Row className={classNames('GlobalSearch', {hasValue: !!value})}>
        <input
          type="textfield"
          className="GlobalSearch-input"
          value={value}
          onChange={onChange}
          onKeyPress={onEnter}
          placeholder={firstUpperTranslated('find meters')}
        />
        {value ? <ContentClear onClick={onClearValue} {...iconProps}/> : <ActionSearch {...iconProps}/>}
      </Row>
    </RowMiddle>
  );
};
