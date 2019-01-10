import {DropDownMenu, MenuItem} from 'material-ui';
import * as React from 'react';
import {colors, fontSize, listItemStyle} from '../../app/themes';
import {OnClick, WithChildren} from '../../types/Types';
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

const listStyle: React.CSSProperties = {
  width: 200,
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

interface Props extends WithChildren {
  value: Period | string;
  menuItems: MenuItemProps[];
}

export interface MenuItemProps {
  value: Period | string;
  label: string;
  primaryText: string;
  onClick: OnClick;
}

export const DropdownMenu = ({children, menuItems, value}: Props) => {

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
        maxHeight={300}
        underlineStyle={underlineStyle}
        labelStyle={labelStyle}
        listStyle={listStyle}
        iconStyle={iconStyle}
        style={style}
        value={value}
        iconButton={<IconCalendar className="IconCalendar"/>}
        selectedMenuItemStyle={selectedMenuItemStyle}
      >
        {renderedMenuItems}
      </DropDownMenu>
      {children}
    </Row>
  );
};
