import {default as classNames} from 'classnames';
import * as React from 'react';
import {colors} from '../../app/colors';
import {ClassNamed, Clickable, Styled, WithChildren} from '../../types/Types';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import {Row} from '../layouts/row/Row';
import {FirstUpper} from '../texts/Texts';

interface Props extends ClassNamed, Clickable, Styled, WithChildren {
  color?: string;
}

export const ButtonLink = withCssStyles(({
  className,
  children,
  color,
  cssStyles: {primary},
  onClick,
  style,
}: Props & ThemeContext) => (
  <Row className={classNames('clickable', className)} onClick={onClick} style={style}>
    <FirstUpper style={{color: color || primary.bg}} className="underline">{children}</FirstUpper>
  </Row>
));

export const ButtonLinkRed = (props: Props) => <ButtonLink {...props} color={colors.notification}/>;
