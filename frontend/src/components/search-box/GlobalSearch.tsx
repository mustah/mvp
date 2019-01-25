import ActionSearch from 'material-ui/svg-icons/action/search';
import ContentClear from 'material-ui/svg-icons/content/clear';
import * as React from 'react';
import {colors} from '../../app/themes';
import {GlobalSearchProps} from '../../containers/GlobalSearchContainer';
import {Row, RowMiddle} from '../layouts/row/Row';
import './GlobalSearch.scss';
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

export const GlobalSearch = ({onChange, onClear, query = ''}: GlobalSearchProps) => {
  const [value, setValue] = React.useState<string>(query);

  const onChangeValue = (event) => {
    event.preventDefault();
    const value = event.target.value;
    setValue(value);
    onChange(value);
  };

  const onClearValue = () => {
    setValue('');
    onClear('');
  };

  return (
    <RowMiddle className="GlobalSearch-Container">
      <Row className="GlobalSearch">
        <input
          type="textfield"
          className="GlobalSearch-input"
          value={value}
          onChange={onChangeValue}
          placeholder="Hitta mätare"
        />
        {value ? <ContentClear onClick={onClearValue} {...iconProps}/> : <ActionSearch {...iconProps}/>}
      </Row>
    </RowMiddle>
  );
};
