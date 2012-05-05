object PatternWidthForm: TPatternWidthForm
  Left = 232
  Top = 165
  Width = 256
  Height = 156
  Caption = 'Musterbreite'
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
  object labelDescription: TLabel
    Left = 8
    Top = 8
    Width = 225
    Height = 26
    Caption = 'Die Musterbreite entspricht dem Umfang der Kette'
    WordWrap = True
  end
  object labelWidth: TLabel
    Left = 8
    Top = 51
    Width = 61
    Height = 13
    Caption = '&Musterbreite:'
    FocusControl = Width
  end
  object Width: TEdit
    Left = 120
    Top = 48
    Width = 25
    Height = 21
    TabOrder = 0
    Text = '10'
  end
  object upWidth: TUpDown
    Left = 145
    Top = 48
    Width = 15
    Height = 21
    Associate = Width
    Min = 5
    Max = 30
    Position = 10
    TabOrder = 1
    Wrap = False
  end
  object bOk: TButton
    Left = 40
    Top = 88
    Width = 73
    Height = 25
    Caption = 'OK'
    Default = True
    ModalResult = 1
    TabOrder = 2
  end
  object bCancel: TButton
    Left = 128
    Top = 88
    Width = 75
    Height = 25
    Cancel = True
    Caption = 'Abbrechen'
    ModalResult = 2
    TabOrder = 3
  end
end
