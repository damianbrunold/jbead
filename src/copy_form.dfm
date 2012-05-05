object CopyForm: TCopyForm
  Left = 443
  Top = 157
  BorderStyle = bsDialog
  Caption = 'Anordnen'
  ClientHeight = 139
  ClientWidth = 210
  Color = clBtnFace
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clWindowText
  Font.Height = -11
  Font.Name = 'MS Sans Serif'
  Font.Style = []
  OldCreateOrder = False
  OnShow = FormShow
  PixelsPerInch = 96
  TextHeight = 13
  object lHorz: TLabel
    Left = 8
    Top = 42
    Width = 97
    Height = 13
    Caption = '&Horizontaler Versatz:'
    FocusControl = horz
  end
  object lVert: TLabel
    Left = 8
    Top = 67
    Width = 85
    Height = 13
    Caption = '&Vertikaler Versatz:'
    FocusControl = vert
  end
  object lCopies: TLabel
    Left = 8
    Top = 18
    Width = 71
    Height = 13
    Caption = '&Anzahl Kopien:'
    FocusControl = Copies
  end
  object horz: TEdit
    Left = 160
    Top = 40
    Width = 25
    Height = 21
    TabOrder = 4
    Text = '5'
  end
  object upHorz: TUpDown
    Left = 185
    Top = 40
    Width = 15
    Height = 21
    Associate = horz
    Min = -100
    Position = 5
    TabOrder = 5
    Wrap = False
  end
  object vert: TEdit
    Left = 160
    Top = 64
    Width = 25
    Height = 21
    TabOrder = 6
    Text = '5'
  end
  object upVert: TUpDown
    Left = 185
    Top = 64
    Width = 15
    Height = 21
    Associate = vert
    Min = -100
    Position = 5
    TabOrder = 7
    Wrap = False
  end
  object Copies: TEdit
    Left = 160
    Top = 16
    Width = 25
    Height = 21
    TabOrder = 2
    Text = '3'
  end
  object upCopies: TUpDown
    Left = 185
    Top = 16
    Width = 15
    Height = 21
    Associate = Copies
    Min = 0
    Position = 3
    TabOrder = 3
    Wrap = False
  end
  object bOK: TButton
    Left = 24
    Top = 104
    Width = 75
    Height = 25
    Caption = 'OK'
    Default = True
    ModalResult = 1
    TabOrder = 0
  end
  object bCancel: TButton
    Left = 112
    Top = 104
    Width = 75
    Height = 25
    Cancel = True
    Caption = 'Abbrechen'
    ModalResult = 2
    TabOrder = 1
  end
end
