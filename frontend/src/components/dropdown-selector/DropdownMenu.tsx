import {DropDownMenu, MenuItem} from 'material-ui';
import SelectField from 'material-ui/SelectField';
import * as React from 'react';
import {colors, dropdownListStyle, fontSize, listItemStyle} from '../../app/themes';
import {firstUpperTranslated} from '../../services/translationService';
import {Quantity} from '../../state/ui/graph/measurement/measurementModels';
import {Children, OnClick, WithChildren} from '../../types/Types';
import {Period} from '../dates/dateModels';
import {IconCalendar} from '../icons/IconCalendar';
import {Row} from '../layouts/row/Row';
import './DropdownMenu.scss';

const height = 32;

const style: React.CSSProperties = {
  height,
  width: 210,
  fontSize: fontSize.normal,
  border: `2px solid ${colors.borderColor}`,
  borderRadius: 4,
  marginLeft: 24,
  marginBottom: 16,
  borderWidth: 1,
};

const underlineStyle: React.CSSProperties = {
  border: 'none',
};

const labelStyle: React.CSSProperties = {
  height,
  lineHeight: 1,
  paddingRight: 0,
  paddingLeft: 8,
  fontSize: 14,
  display: 'flex',
  alignItems: 'center',
  width: 210,
};

const iconStyle: React.CSSProperties = {
  fill: colors.lightBlack,
  height,
  width: 36,
  right: 0,
  top: 0,
  padding: 0,
};

const selectedMenuItemStyle: React.CSSProperties = {color: colors.blue};

export interface DropdownMenuProps extends WithChildren {
  disabled?: boolean;
  menuItems: MenuItemProps[];
  value?: Period | string;
}

export interface MenuItemProps {
  checked?: boolean;
  label: string;
  onClick: OnClick;
  primaryText: string;
  value: Period | string;
}

export const DropdownMenu = ({children, menuItems, value}: DropdownMenuProps) => {

  const renderedMenuItems = menuItems.map(({primaryText, label, value, onClick}: MenuItemProps) => (
    <MenuItem
      className="DropdownMenu-MenuItem"
      key={primaryText}
      label={label}
      primaryText={primaryText}
      style={listItemStyle}
      value={value}
      onClick={onClick}
    />
  ));

  return (
    <Row className="DropdownMenu">
      <DropDownMenu
        className="DropdownMenu-dropdown"
        iconButton={<IconCalendar className="IconCalendar"/>}
        iconStyle={iconStyle}
        maxHeight={300}
        labelStyle={labelStyle}
        listStyle={dropdownListStyle}
        selectedMenuItemStyle={selectedMenuItemStyle}
        style={style}
        underlineStyle={underlineStyle}
        value={value}
      >
        {renderedMenuItems}
      </DropDownMenu>
      {children}
    </Row>
  );
};

const multiSelectLabelStyle: React.CSSProperties = {
  ...labelStyle,
  height: 38,
  width: 168,
};

const multiSelectStyle: React.CSSProperties = {
  ...style,
  marginLeft: 0,
  marginBottom: 0,
  borderColor: colors.borderColor,
};

const hintStyle: React.CSSProperties = {bottom: 5, left: 8, color: colors.lightBlack};

export interface MultiSelectDropdownMenuProps {
  selectedQuantities: Quantity[];
  changeQuantities: (event, index, values) => void;
  children?: Children;
}

export const MultiSelectDropdownMenu =
  ({children, changeQuantities, selectedQuantities}: MultiSelectDropdownMenuProps) => (
    <Row className="DropdownMenu">
      <SelectField
        autoWidth={true}
        className="DropdownMenu-dropdown"
        hintStyle={hintStyle}
        hintText={firstUpperTranslated('select quantities')}
        iconStyle={iconStyle}
        labelStyle={multiSelectLabelStyle}
        multiple={true}
        onChange={changeQuantities}
        selectedMenuItemStyle={selectedMenuItemStyle}
        style={multiSelectStyle}
        value={selectedQuantities}
        underlineStyle={underlineStyle}
      >
        {children}
      </SelectField>
    </Row>
  );
