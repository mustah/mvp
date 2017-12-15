import FlatButton from 'material-ui/FlatButton';
import ActionInfoOutline from 'material-ui/svg-icons/action/info-outline';
import * as React from 'react';
import {colors} from '../../app/themes';
import {OnClick} from '../../types/Types';

interface InfoLinkProps {
  onClick: OnClick;
  label: string | number;
}

const labelStyle = {paddingLeft: 8};

export const ButtonInfoLink = ({label, onClick}: InfoLinkProps) => (
  <FlatButton
    hoverColor="inherit"
    icon={<ActionInfoOutline color={colors.lightBlack} hoverColor={colors.iconHover}/>}
    label={label}
    labelPosition="before"
    labelStyle={labelStyle}
    onClick={onClick}
  />
);
