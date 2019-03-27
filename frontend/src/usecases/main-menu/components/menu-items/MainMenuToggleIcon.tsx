import {default as classNames} from 'classnames';
import {SvgIconProps} from 'material-ui';
import FlatButton from 'material-ui/FlatButton';
import NavigationChevronLeft from 'material-ui/svg-icons/navigation/chevron-left';
import NavigationChevronRight from 'material-ui/svg-icons/navigation/chevron-right';
import * as React from 'react';
import {border, boxShadow, colors, iconStyle, svgIconProps} from '../../../../app/themes';
import {RowRight} from '../../../../components/layouts/row/Row';
import {translate} from '../../../../services/translationService';
import {OnClick} from '../../../../types/Types';
import './MainMenuToggleIcon.scss';

interface Props {
  onClick: OnClick;
  isSideMenuOpen: boolean;
}

const style: React.CSSProperties = {
  ...iconStyle,
  cursor: 'pointer',
};

const buttonStyle: React.CSSProperties = {
  minWidth: 44,
  height: 44,
  borderRadius: 44 / 2,
  border,
  backgroundColor: colors.white,
  boxShadow,
};

export const MainMenuToggleIcon = ({onClick, isSideMenuOpen}: Props) => {
  const iconsProps: SvgIconProps = {style, ...svgIconProps};

  const renderArrow = isSideMenuOpen
    ? (<NavigationChevronLeft {...iconsProps}/>)
    : (<NavigationChevronRight {...iconsProps}/>);

  return (
    <RowRight className={classNames('MainMenuToggleIcon', {isSideMenuOpen})}>
      <FlatButton
        className="MainMenuToggleIcon-Button"
        onClick={onClick}
        icon={renderArrow}
        style={buttonStyle}
        label={isSideMenuOpen ? translate('hide menu') : null}
        labelPosition="before"
      />
    </RowRight>
  );
};
