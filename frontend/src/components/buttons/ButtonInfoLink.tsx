import FlatButton from 'material-ui/FlatButton';
import ActionInfoOutline from 'material-ui/svg-icons/action/info-outline';
import * as React from 'react';
import {colors} from '../../app/themes';
import {OnClick} from '../../types/Types';

interface InfoLinkProps {
  onClick: OnClick;
  label: string | number;
}

const labelStyle: React.CSSProperties = {paddingLeft: 8};
const iconStyle: React.CSSProperties = {marginLeft: 4};

export const ButtonInfoLink = ({label, onClick}: InfoLinkProps) => (
  <FlatButton
    hoverColor="inherit"
    icon={<ActionInfoOutline style={iconStyle} color={colors.lightBlack} hoverColor={colors.iconHover}/>}
    label={label}
    labelPosition="after"
    labelStyle={labelStyle}
    onClick={onClick}
  />
);
